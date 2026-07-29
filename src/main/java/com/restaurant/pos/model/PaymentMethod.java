package com.restaurant.pos.model;

public enum PaymentMethod {
    CASH("Cash"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
