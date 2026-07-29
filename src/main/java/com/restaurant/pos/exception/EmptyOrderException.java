package com.restaurant.pos.exception;

public class EmptyOrderException extends PosException {

    public EmptyOrderException() {
        super("Order must contain at least one item.");
    }
}
