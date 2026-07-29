package com.restaurant.pos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return new Money(amount);
    }

    public static Money of(long majorUnits) {
        return new Money(BigDecimal.valueOf(majorUnits));
    }

    public static Money ofMinorUnits(long minorUnits) {
        return new Money(BigDecimal.valueOf(minorUnits, SCALE));
    }

    public static Money parse(String text) {
        Objects.requireNonNull(text, "text");
        return new Money(new BigDecimal(text.trim()));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money percentageOf(BigDecimal percent) {
        Objects.requireNonNull(percent, "percent");
        BigDecimal rate = percent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return new Money(this.amount.multiply(rate));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public BigDecimal toBigDecimal() {
        return amount;
    }

    public String toPlainString() {
        return amount.toPlainString();
    }

    public long toMinorUnits() {
        return amount.movePointRight(SCALE).longValueExact();
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money other)) {
            return false;
        }
        return this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {

        return amount.hashCode();
    }

    @Override
    public String toString() {
        return toPlainString();
    }
}
