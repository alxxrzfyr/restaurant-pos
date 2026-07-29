package com.restaurant.pos.model;

import java.util.Objects;

public final class CheckoutResult {

    private final Order order;
    private final Payment payment;

    public CheckoutResult(Order order, Payment payment) {
        this.order = Objects.requireNonNull(order, "order");
        this.payment = Objects.requireNonNull(payment, "payment");
    }

    public Order order() {
        return order;
    }

    public Payment payment() {
        return payment;
    }

    public Money change() {
        return payment.changeGiven();
    }
}
