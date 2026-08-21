package com.restaurant.pos.service;

import com.restaurant.pos.exception.EmptyOrderException;
import com.restaurant.pos.exception.InsufficientPaymentException;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.Cart;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderLineItem;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.model.PaymentMethod;
import com.restaurant.pos.repository.OrderRepository;
import com.restaurant.pos.repository.PaymentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SettingsService settingsService;
    private final PaymentProcessor paymentProcessor;
    private final AuditService auditService;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository,
                         SettingsService settingsService, PaymentProcessor paymentProcessor, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.settingsService = settingsService;
        this.paymentProcessor = paymentProcessor;
        this.auditService = auditService;
    }

    public OrderTotals calculateTotals(Cart cart) {
        Money subtotal = cart.subtotal();
        Money discount = cart.discount();
        Money taxableAmount = subtotal.subtract(discount);
        var vatRate = settingsService.currentVatRatePercent();
        Money vat = taxableAmount.percentageOf(vatRate);
        Money totalDue = taxableAmount.add(vat);
        return new OrderTotals(subtotal, discount, vatRate, vat, totalDue);
    }

    public Money calculateChange(Money totalDue, Money amountTendered) {
        if (amountTendered.compareTo(totalDue) < 0) {
            throw new InsufficientPaymentException(totalDue, amountTendered);
        }
        return amountTendered.subtract(totalDue);
    }

    public CheckoutResult checkout(Cart cart, long cashierId, String cashierName,
                                    PaymentMethod paymentMethod, Money amountTendered) {
        if (cart.isEmpty()) {
            throw new EmptyOrderException();
        }

        OrderTotals totals = calculateTotals(cart);
        Money change = calculateChange(totals.totalDue(), amountTendered);

        String transactionId = null;
        if (paymentMethod != PaymentMethod.CASH) {
            transactionId = paymentProcessor.processPayment(paymentMethod, amountTendered);
        }

        Instant now = Instant.now();

        List<OrderLineItem> lineItems = cart.lines().stream().map(OrderLineItem::fromCartLine).toList();

        Order order = Order.builder()
                .cashierId(cashierId)
                .cashierName(cashierName)
                .orderType(cart.orderType())
                .tableNumber(cart.tableNumber())
                .notes(cart.notes())
                .lineItems(lineItems)
                .subtotal(totals.subtotal())
                .discountAmount(totals.discount())
                .vatRatePercent(totals.vatRatePercent())
                .vatAmount(totals.vat())
                .totalDue(totals.totalDue())
                .status(OrderStatus.PAID)
                .placedAt(now)
                .build();

        Payment payment = Payment.builder()
                .method(paymentMethod)
                .amountTendered(amountTendered)
                .changeGiven(change)
                .referenceNumber(transactionId)
                .paidAt(now)
                .build();

        CheckoutResult result = orderRepository.placeOrder(order, payment);
        auditService.record(AuditEventType.ORDER_PLACED, cashierId, cashierName,
                "order " + result.order().orderNumber() + " total " + totals.totalDue());
        return result;
    }

    public void voidOrder(long orderId, long voidedByUserId, String voidedByUsername, String reason) {
        orderRepository.markVoided(orderId, voidedByUserId, reason, Instant.now());
        auditService.record(AuditEventType.ORDER_VOIDED, voidedByUserId, voidedByUsername,
                "order id=" + orderId + " reason=" + reason);
    }

    public List<Order> findOrderHistory(Instant from, Instant to) {
        return orderRepository.findByDateRange(from, to);
    }

    public Optional<Order> findOrderById(long orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<Payment> findPaymentForOrder(long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
