package com.restaurant.pos.model;

public enum OrderType {
    DINE_IN("Dine-In"),
    TAKE_OUT("Take-Out");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
