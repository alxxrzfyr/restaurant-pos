package com.restaurant.pos.service;

import com.restaurant.pos.exception.EmptyOrderException;
import com.restaurant.pos.exception.InsufficientPaymentException;
import com.restaurant.pos.model.Cart;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.OrderType;
import com.restaurant.pos.model.PaymentMethod;
import com.restaurant.pos.testutil.InMemoryAuditEventRepository;
import com.restaurant.pos.testutil.InMemoryOrderRepository;
import com.restaurant.pos.testutil.InMemoryPaymentRepository;
import com.restaurant.pos.testutil.InMemorySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private InMemoryOrderRepository orderRepository;
    private InMemorySettingsRepository settingsRepository;
    private OrderService orderService;

    private MenuItem burger;
    private MenuItem soda;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
        settingsRepository = new InMemorySettingsRepository();
        settingsRepository.set("business.vatRatePercent", "12.00");

        AuditService auditService = new AuditService(new InMemoryAuditEventRepository());
        com.restaurant.pos.repository.UserRepository userRepository = new com.restaurant.pos.testutil.InMemoryUserRepository();
        userRepository.insert(com.restaurant.pos.model.User.builder().id(1L).username("admin").displayName("Admin").role(com.restaurant.pos.model.Role.ADMINISTRATOR).passwordHash("hash").active(true).build());
        com.restaurant.pos.config.AppConfig config = new com.restaurant.pos.config.AppConfig();
        AuthService authService = new AuthService(userRepository, auditService, config, null);
        SettingsService settingsService = new SettingsService(settingsRepository, authService, auditService);
        orderService = new OrderService(orderRepository, new InMemoryPaymentRepository(), settingsService, new MockPaymentProcessor(), auditService);

        burger = MenuItem.builder().id(1L).name("Burger").price(Money.of(new BigDecimal("30.00"))).categoryId(1L).build();
        soda = MenuItem.builder().id(2L).name("Soda").price(Money.of(new BigDecimal("25.00"))).categoryId(2L).build();
    }

    private Cart cartWithBurgerAndSoda() {
        Cart cart = new Cart();
        cart.addItem(burger);
        cart.addItem(soda);
        cart.addItem(soda);
        cart.setOrderType(OrderType.DINE_IN);
        return cart;
    }

    @Test
    void calculateTotalsAppliesVatOnTopOfSubtotal() {
        Cart cart = cartWithBurgerAndSoda();
        OrderTotals totals = orderService.calculateTotals(cart);

        assertEquals(Money.of(new BigDecimal("80.00")), totals.subtotal());
        assertEquals(Money.of(new BigDecimal("9.60")), totals.vat());
        assertEquals(Money.of(new BigDecimal("89.60")), totals.totalDue());
    }

    @Test
    void calculateTotalsAppliesDiscountBeforeVat() {
        Cart cart = cartWithBurgerAndSoda();
        cart.setDiscount(Money.of(new BigDecimal("10.00")));

        OrderTotals totals = orderService.calculateTotals(cart);

        assertEquals(Money.of(new BigDecimal("8.40")), totals.vat());
        assertEquals(Money.of(new BigDecimal("78.40")), totals.totalDue());
    }

    @Test
    void calculateChangeReturnsDifferenceWhenTenderedCoversTotal() {
        Money change = orderService.calculateChange(Money.of(new BigDecimal("89.60")), Money.of(new BigDecimal("100.00")));
        assertEquals(Money.of(new BigDecimal("10.40")), change);
    }

    @Test
    void calculateChangeThrowsWhenTenderedIsInsufficient() {
        InsufficientPaymentException ex = assertThrows(InsufficientPaymentException.class,
                () -> orderService.calculateChange(Money.of(new BigDecimal("89.60")), Money.of(new BigDecimal("50.00"))));
        assertEquals(Money.of(new BigDecimal("89.60")), ex.amountDue());
    }

    @Test
    void checkoutRejectsEmptyCart() {
        assertThrows(EmptyOrderException.class, () -> orderService.checkout(
                new Cart(), 1L, "cashier1", PaymentMethod.CASH, Money.of(new BigDecimal("100.00"))));
    }

    @Test
    void checkoutPersistsOrderWithGeneratedOrderNumber() {
        Cart cart = cartWithBurgerAndSoda();
        CheckoutResult result = orderService.checkout(cart, 7L, "cashier1", PaymentMethod.CASH, Money.of(new BigDecimal("100.00")));

        assertEquals("0001", result.order().orderNumber());
        assertEquals(Money.of(new BigDecimal("10.40")), result.change());
        assertEquals(2, result.order().lineItems().size());
    }

    @Test
    void checkoutGeneratesSequentialOrderNumbersAcrossOrders() {
        orderService.checkout(cartWithBurgerAndSoda(), 7L, "cashier1", PaymentMethod.CASH, Money.of(new BigDecimal("100.00")));
        CheckoutResult second = orderService.checkout(cartWithBurgerAndSoda(), 7L, "cashier1", PaymentMethod.CASH, Money.of(new BigDecimal("100.00")));

        assertEquals("0002", second.order().orderNumber());
    }

    @Test
    void checkoutWithExactAmountGivesZeroChange() {
        Cart cart = cartWithBurgerAndSoda();
        OrderTotals totals = orderService.calculateTotals(cart);
        CheckoutResult result = orderService.checkout(cart, 7L, "cashier1", PaymentMethod.CASH, totals.totalDue());

        assertTrue(result.change().isZero());
    }
}
