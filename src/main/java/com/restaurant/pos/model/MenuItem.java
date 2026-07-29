package com.restaurant.pos.model;

import java.util.Objects;

public final class MenuItem {

    private final Long id;
    private final String name;
    private final Money price;
    private final Money cost;
    private final Long categoryId;
    private final String categoryName;
    private final boolean available;

    private final String imagePath;

    private MenuItem(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.price = Objects.requireNonNull(builder.price, "price is required");
        this.cost = builder.cost;
        this.categoryId = Objects.requireNonNull(builder.categoryId, "categoryId is required");
        this.categoryName = builder.categoryName;
        this.available = builder.available;
        this.imagePath = builder.imagePath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .price(price)
                .cost(cost)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .available(available)
                .imagePath(imagePath);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public Money cost() {
        return cost;
    }

    public Long categoryId() {
        return categoryId;
    }

    public String categoryName() {
        return categoryName;
    }

    public boolean available() {
        return available;
    }

    public String imagePath() {
        return imagePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MenuItem other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }

    public static final class Builder {
        private Long id;
        private String name;
        private Money price;
        private Money cost;
        private Long categoryId;
        private String categoryName;
        private boolean available = true;
        private String imagePath;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(Money price) {
            this.price = price;
            return this;
        }

        public Builder cost(Money cost) {
            this.cost = cost;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public MenuItem build() {
            return new MenuItem(this);
        }
    }
}
