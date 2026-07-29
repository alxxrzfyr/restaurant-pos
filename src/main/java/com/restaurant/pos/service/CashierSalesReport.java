package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;

public record CashierSalesReport(String cashierName, int orderCount, Money totalSales) {
}
