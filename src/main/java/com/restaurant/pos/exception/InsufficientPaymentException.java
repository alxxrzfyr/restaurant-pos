package com.restaurant.pos.exception;

import com.restaurant.pos.model.Money;

public class InsufficientPaymentException extends PosException {

    private final Money amountDue;
    private final Money amountTendered;

    public InsufficientPaymentException(Money amountDue, Money amountTendered) {
        super("Amount tendered (" + amountTendered + ") is less than the amount due (" + amountDue + ")");
        this.amountDue = amountDue;
        this.amountTendered = amountTendered;
    }

    public Money amountDue() {
        return amountDue;
    }

    public Money amountTendered() {
        return amountTendered;
    }
}
