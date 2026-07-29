package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderTotals(Money subtotal, Money discount, BigDecimal vatRatePercent, Money vat, Money totalDue) {

    public OrderTotals {
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(vatRatePercent, "vatRatePercent");
        Objects.requireNonNull(vat, "vat");
        Objects.requireNonNull(totalDue, "totalDue");
    }
}
