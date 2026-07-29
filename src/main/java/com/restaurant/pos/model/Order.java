package com.restaurant.pos.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Order {

    private static final int ORDER_NUMBER_MIN_DIGITS = 4;

    private final Long id;
    private final Long cashierId;
    private final String cashierName;
    private final OrderType orderType;
    private final String tableNumber;
    private final String notes;
    private final List<OrderLineItem> lineItems;
    private final Money subtotal;
    private final Money discountAmount;
    private final BigDecimal vatRatePercent;
    private final Money vatAmount;
    private final Money totalDue;
    private final OrderStatus status;
    private final Instant placedAt;
    private final Instant voidedAt;
    private final Long voidedByUserId;
    private final String voidReason;

    private Order(Builder builder) {
        this.id = builder.id;
        this.cashierId = Objects.requireNonNull(builder.cashierId, "cashierId is required");
        this.cashierName = Objects.requireNonNull(builder.cashierName, "cashierName is required");
        this.orderType = Objects.requireNonNull(builder.orderType, "orderType is required");
        this.tableNumber = builder.tableNumber;
        this.notes = builder.notes;
        this.lineItems = List.copyOf(Objects.requireNonNull(builder.lineItems, "lineItems is required"));
        this.subtotal = Objects.requireNonNull(builder.subtotal, "subtotal is required");
        this.discountAmount = Objects.requireNonNull(builder.discountAmount, "discountAmount is required");
        this.vatRatePercent = Objects.requireNonNull(builder.vatRatePercent, "vatRatePercent is required");
        this.vatAmount = Objects.requireNonNull(builder.vatAmount, "vatAmount is required");
        this.totalDue = Objects.requireNonNull(builder.totalDue, "totalDue is required");
        this.status = Objects.requireNonNull(builder.status, "status is required");
        this.placedAt = Objects.requireNonNull(builder.placedAt, "placedAt is required");
        this.voidedAt = builder.voidedAt;
        this.voidedByUserId = builder.voidedByUserId;
        this.voidReason = builder.voidReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .cashierId(cashierId)
                .cashierName(cashierName)
                .orderType(orderType)
                .tableNumber(tableNumber)
                .notes(notes)
                .lineItems(lineItems)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .vatRatePercent(vatRatePercent)
                .vatAmount(vatAmount)
                .totalDue(totalDue)
                .status(status)
                .placedAt(placedAt)
                .voidedAt(voidedAt)
                .voidedByUserId(voidedByUserId)
                .voidReason(voidReason);
    }

    public int itemCount() {
        int count = 0;
        for (OrderLineItem line : lineItems) {
            count += line.quantity();
        }
        return count;
    }

    public Long id() {
        return id;
    }

    public String orderNumber() {
        Objects.requireNonNull(id, "order has not been persisted yet");
        return String.format("%0" + ORDER_NUMBER_MIN_DIGITS + "d", id);
    }

    public Long cashierId() {
        return cashierId;
    }

    public String cashierName() {
        return cashierName;
    }

    public OrderType orderType() {
        return orderType;
    }

    public String tableNumber() {
        return tableNumber;
    }

    public String notes() {
        return notes;
    }

    public List<OrderLineItem> lineItems() {
        return lineItems;
    }

    public Money subtotal() {
        return subtotal;
    }

    public Money discountAmount() {
        return discountAmount;
    }

    public BigDecimal vatRatePercent() {
        return vatRatePercent;
    }

    public Money vatAmount() {
        return vatAmount;
    }

    public Money totalDue() {
        return totalDue;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant placedAt() {
        return placedAt;
    }

    public Instant voidedAt() {
        return voidedAt;
    }

    public Long voidedByUserId() {
        return voidedByUserId;
    }

    public String voidReason() {
        return voidReason;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Order other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static final class Builder {
        private Long id;
        private Long cashierId;
        private String cashierName;
        private OrderType orderType;
        private String tableNumber;
        private String notes;
        private List<OrderLineItem> lineItems = List.of();
        private Money subtotal;
        private Money discountAmount = Money.ZERO;
        private BigDecimal vatRatePercent;
        private Money vatAmount;
        private Money totalDue;
        private OrderStatus status = OrderStatus.OPEN;
        private Instant placedAt;
        private Instant voidedAt;
        private Long voidedByUserId;
        private String voidReason;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder cashierId(Long cashierId) {
            this.cashierId = cashierId;
            return this;
        }

        public Builder cashierName(String cashierName) {
            this.cashierName = cashierName;
            return this;
        }

        public Builder orderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder tableNumber(String tableNumber) {
            this.tableNumber = tableNumber;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder lineItems(List<OrderLineItem> lineItems) {
            this.lineItems = lineItems;
            return this;
        }

        public Builder subtotal(Money subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder discountAmount(Money discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder vatRatePercent(BigDecimal vatRatePercent) {
            this.vatRatePercent = vatRatePercent;
            return this;
        }

        public Builder vatAmount(Money vatAmount) {
            this.vatAmount = vatAmount;
            return this;
        }

        public Builder totalDue(Money totalDue) {
            this.totalDue = totalDue;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder placedAt(Instant placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Builder voidedAt(Instant voidedAt) {
            this.voidedAt = voidedAt;
            return this;
        }

        public Builder voidedByUserId(Long voidedByUserId) {
            this.voidedByUserId = voidedByUserId;
            return this;
        }

        public Builder voidReason(String voidReason) {
            this.voidReason = voidReason;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
