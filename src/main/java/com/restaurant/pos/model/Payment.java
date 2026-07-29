package com.restaurant.pos.model;

import java.time.Instant;
import java.util.Objects;

public final class Payment {

    private final Long id;
    private final Long orderId;
    private final PaymentMethod method;
    private final Money amountTendered;
    private final Money changeGiven;
    private final String referenceNumber;
    private final Instant paidAt;

    private Payment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.method = Objects.requireNonNull(builder.method, "method is required");
        this.amountTendered = Objects.requireNonNull(builder.amountTendered, "amountTendered is required");
        this.changeGiven = Objects.requireNonNull(builder.changeGiven, "changeGiven is required");
        this.referenceNumber = builder.referenceNumber;
        this.paidAt = Objects.requireNonNull(builder.paidAt, "paidAt is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .orderId(orderId)
                .method(method)
                .amountTendered(amountTendered)
                .changeGiven(changeGiven)
                .referenceNumber(referenceNumber)
                .paidAt(paidAt);
    }

    public Long id() {
        return id;
    }

    public Long orderId() {
        return orderId;
    }

    public PaymentMethod method() {
        return method;
    }

    public Money amountTendered() {
        return amountTendered;
    }

    public Money changeGiven() {
        return changeGiven;
    }

    public String referenceNumber() {
        return referenceNumber;
    }

    public Instant paidAt() {
        return paidAt;
    }

    public static final class Builder {
        private Long id;
        private Long orderId;
        private PaymentMethod method;
        private Money amountTendered;
        private Money changeGiven;
        private String referenceNumber;
        private Instant paidAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder amountTendered(Money amountTendered) {
            this.amountTendered = amountTendered;
            return this;
        }

        public Builder changeGiven(Money changeGiven) {
            this.changeGiven = changeGiven;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder paidAt(Instant paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
