package com.restaurant.pos.model;

import java.util.Objects;

public final class CartLine {

    private final MenuItem menuItem;
    private final int quantity;

    CartLine(MenuItem menuItem, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1, got " + quantity);
        }
        this.menuItem = Objects.requireNonNull(menuItem, "menuItem");
        this.quantity = quantity;
    }

    public MenuItem menuItem() {
        return menuItem;
    }

    public int quantity() {
        return quantity;
    }

    public Money lineTotal() {
        return menuItem.price().multiply(quantity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CartLine other)) {
            return false;
        }
        return quantity == other.quantity && menuItem.equals(other.menuItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItem, quantity);
    }
}
