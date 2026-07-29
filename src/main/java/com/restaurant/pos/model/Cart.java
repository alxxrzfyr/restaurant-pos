package com.restaurant.pos.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Cart {

    private final Map<Long, CartLine> lines = new LinkedHashMap<>();
    private OrderType orderType;
    private String tableNumber;
    private String notes;
    private Money discount = Money.ZERO;

    public void addItem(MenuItem item) {
        adjustQuantity(item, 1);
    }

    public void removeOneUnit(MenuItem item) {
        adjustQuantity(item, -1);
    }

    public void setQuantity(MenuItem item, int quantity) {
        Objects.requireNonNull(item, "item");
        if (quantity <= 0) {
            lines.remove(item.id());
            return;
        }
        lines.put(item.id(), new CartLine(item, quantity));
    }

    public void removeLine(MenuItem item) {
        Objects.requireNonNull(item, "item");
        lines.remove(item.id());
    }

    private void adjustQuantity(MenuItem item, int delta) {
        Objects.requireNonNull(item, "item");
        int currentQuantity = quantityOf(item);
        setQuantity(item, currentQuantity + delta);
    }

    public int quantityOf(MenuItem item) {
        Objects.requireNonNull(item, "item");
        CartLine line = lines.get(item.id());
        return line == null ? 0 : line.quantity();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int lineCount() {
        return lines.size();
    }

    public List<CartLine> lines() {
        return new ArrayList<>(lines.values());
    }

    public Money subtotal() {
        Money total = Money.ZERO;
        for (CartLine line : lines.values()) {
            total = total.add(line.lineTotal());
        }
        return total;
    }

    public Money discount() {
        return discount;
    }

    public void setDiscount(Money discount) {
        this.discount = Objects.requireNonNull(discount, "discount");
    }

    public OrderType orderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String tableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String notes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void clear() {
        lines.clear();
        orderType = null;
        tableNumber = null;
        notes = null;
        discount = Money.ZERO;
    }
}
