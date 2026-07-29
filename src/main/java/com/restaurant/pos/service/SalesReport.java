package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record SalesReport(Instant from, Instant to, int orderCount, int itemsSold,
                           Money grossRevenue, Money vatCollected, Money netRevenue,
                           List<TopSellingItem> topSellingItems) {

    public SalesReport {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(grossRevenue, "grossRevenue");
        Objects.requireNonNull(vatCollected, "vatCollected");
        Objects.requireNonNull(netRevenue, "netRevenue");
        topSellingItems = List.copyOf(Objects.requireNonNull(topSellingItems, "topSellingItems"));
    }
}
