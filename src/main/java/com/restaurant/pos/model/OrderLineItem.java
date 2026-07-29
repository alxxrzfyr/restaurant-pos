package com.restaurant.pos.model;

import java.util.Objects;

public final class OrderLineItem {

    private final Long id;
    private final Long menuItemId;
    private final String itemName;
    private final Money unitPrice;
    private final int quantity;

    private OrderLineItem(Builder builder) {
        this.id = builder.id;
        this.menuItemId = builder.menuItemId;
        this.itemName = Objects.requireNonNull(builder.itemName, "itemName is required");
        this.unitPrice = Objects.requireNonNull(builder.unitPrice, "unitPrice is required");
        if (builder.quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1, got " + builder.quantity);
        }
        this.quantity = builder.quantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OrderLineItem fromCartLine(CartLine cartLine) {
        return builder()
                .menuItemId(cartLine.menuItem().id())
                .itemName(cartLine.menuItem().name())
                .unitPrice(cartLine.menuItem().price())
                .quantity(cartLine.quantity())
                .build();
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    public Long id() {
        return id;
    }

    public Long menuItemId() {
        return menuItemId;
    }

    public String itemName() {
        return itemName;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }

    public static final class Builder {
        private Long id;
        private Long menuItemId;
        private String itemName;
        private Money unitPrice;
        private int quantity;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder menuItemId(Long menuItemId) {
            this.menuItemId = menuItemId;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder unitPrice(Money unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderLineItem build() {
            return new OrderLineItem(this);
        }
    }
}
