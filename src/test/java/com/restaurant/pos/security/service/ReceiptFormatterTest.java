package com.restaurant.pos.service;

import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderLineItem;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.OrderType;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.model.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    @Test
    @DisplayName("Every line in formatted receipt must be <= 42 printable characters")
    void testMaxLineWidthConstraint() {
        BusinessSettings settings = BusinessSettings.builder()
                .businessName("Mang Inasal Classic Grill Restaurant")
                .branchName("BGC High Street Branch 102")
                .address("Unit 101 Ground Floor Building A Bonifacio High Street Taguig City Metro Manila")
                .phone("09171234567")
                .tin("123-456-789-00000")
                .vatRegNo("VAT-987654321-V")
                .birPermitNo("BIR-PERMIT-2026-000123")
                .posSerialNo("SN-POS-88990011")
                .machineNo("MIN-100200300400")
                .vatRatePercent(new BigDecimal("12.00"))
                .build();

        OrderLineItem item1 = OrderLineItem.builder()
                .id(1L)
                .itemName("Chicken Inasal Pecho Large Combo Meal with Unlimited Extra Rice")
                .unitPrice(Money.of(250))
                .quantity(2)
                .build();

        OrderLineItem item2 = OrderLineItem.builder()
                .id(2L)
                .itemName("Halo-Halo Extra Special Supreme with Ice Cream")
                .unitPrice(Money.of(120))
                .quantity(1)
                .build();

        Order order = Order.builder()
                .id(1042L)
                .cashierId(1L)
                .cashierName("Maria Santos Cashier")
                .orderType(OrderType.DINE_IN)
                .tableNumber("Table 15")
                .lineItems(List.of(item1, item2))
                .subtotal(Money.of(620))
                .discountAmount(Money.of(20))
                .vatRatePercent(new BigDecimal("12.00"))
                .vatAmount(Money.of(72))
                .totalDue(Money.of(672))
                .status(OrderStatus.PAID)
                .placedAt(Instant.now())
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .orderId(1042L)
                .method(PaymentMethod.CASH)
                .amountTendered(Money.of(1000))
                .changeGiven(Money.of(328))
                .paidAt(Instant.now())
                .build();

        String receipt = ReceiptFormatter.formatReceipt(order, payment, settings);
        String[] lines = receipt.split("\n");

        for (String line : lines) {
            assertTrue(line.length() <= 42,
                    "Line exceeds 42 characters (" + line.length() + " chars): [" + line + "]");
        }
    }

    @Test
    @DisplayName("Verify PHP label is present only on TOTAL, Cash Tendered, and Change lines")
    void testCurrencyLabelPlacement() {
        BusinessSettings settings = BusinessSettings.builder()
                .businessName("Simple Diner")
                .vatRatePercent(new BigDecimal("12.00"))
                .build();

        OrderLineItem item = OrderLineItem.builder()
                .id(1L)
                .itemName("Burger")
                .unitPrice(Money.of(120))
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(101L)
                .cashierId(1L)
                .cashierName("Admin")
                .orderType(OrderType.TAKE_OUT)
                .lineItems(List.of(item))
                .subtotal(Money.of(240))
                .vatRatePercent(new BigDecimal("12.00"))
                .vatAmount(Money.of(2880).percentageOf(new BigDecimal("1.00")))
                .totalDue(Money.of(240))
                .status(OrderStatus.PAID)
                .placedAt(Instant.now())
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .orderId(101L)
                .method(PaymentMethod.CASH)
                .amountTendered(Money.of(500))
                .changeGiven(Money.of(260))
                .paidAt(Instant.now())
                .build();

        String receipt = ReceiptFormatter.formatReceipt(order, payment, settings);
        String[] lines = receipt.split("\n");

        for (String line : lines) {
            if (line.startsWith("2   Burger")) {
                assertFalse(line.contains("PHP"), "Item line should not contain PHP label: " + line);
            }
            if (line.startsWith("TOTAL")) {
                assertTrue(line.contains("PHP 240.00"), "TOTAL line must contain PHP label: " + line);
            }
            if (line.startsWith("Cash Tendered")) {
                assertTrue(line.contains("PHP 500.00"), "Cash Tendered line must contain PHP label: " + line);
            }
            if (line.startsWith("Change")) {
                assertTrue(line.contains("PHP 260.00"), "Change line must contain PHP label: " + line);
            }
        }
    }

    @Test
    @DisplayName("Verify dynamic inclusion of BIR details when configured in BusinessSettings")
    void testBirDetailsRendering() {
        BusinessSettings settings = BusinessSettings.builder()
                .businessName("Tasty Eats")
                .tin("111-222-333-000")
                .vatRegNo("VAT-111222")
                .birPermitNo("BIR-PERMIT-999")
                .vatRatePercent(new BigDecimal("12.00"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .cashierId(1L)
                .cashierName("Cashier1")
                .orderType(OrderType.DINE_IN)
                .lineItems(List.of())
                .subtotal(Money.ZERO)
                .vatRatePercent(new BigDecimal("12.00"))
                .vatAmount(Money.ZERO)
                .totalDue(Money.ZERO)
                .status(OrderStatus.PAID)
                .placedAt(Instant.now())
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .method(PaymentMethod.CASH)
                .amountTendered(Money.ZERO)
                .changeGiven(Money.ZERO)
                .paidAt(Instant.now())
                .build();

        String receipt = ReceiptFormatter.formatReceipt(order, payment, settings);

        assertTrue(receipt.contains("TIN: 111-222-333-000"));
        assertTrue(receipt.contains("VAT REG: VAT-111222"));
        assertTrue(receipt.contains("BIR PERMIT #: BIR-PERMIT-999"));
    }

    @Test
    @DisplayName("Verify formatPair aligns labels and values to exactly 42 characters")
    void testFormatPairAlignment() {
        String formatted = ReceiptFormatter.formatPair("Cash Tendered", "PHP 1,000.00");
        assertEquals(42, formatted.length());
        assertTrue(formatted.startsWith("Cash Tendered"));
        assertTrue(formatted.endsWith("PHP 1,000.00"));
    }
}
