package com.restaurant.pos.model;

import java.util.Objects;

public final class Category {

    private final Long id;
    private final String name;
    private final int displayOrder;

    private Category(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.displayOrder = builder.displayOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().id(id).name(name).displayOrder(displayOrder);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int displayOrder() {
        return displayOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Category other)) {
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
        private int displayOrder;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder displayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Category build() {
            return new Category(this);
        }
    }
}
