package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;

public record TopSellingItem(String itemName, int quantitySold, Money revenue) {
}
