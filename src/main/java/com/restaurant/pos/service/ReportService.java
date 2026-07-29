package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderLineItem;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.OrderRepository;
import com.restaurant.pos.repository.UserRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReportService {

    private static final int TOP_SELLING_ITEMS_LIMIT = 10;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReportService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public SalesReport salesReport(Instant from, Instant to) {
        List<Order> orders = orderRepository.findByStatusAndDateRange(OrderStatus.PAID, from, to);

        Money gross = Money.ZERO;
        Money vat = Money.ZERO;
        int itemsSold = 0;
        Map<String, TopSellingItemAccumulator> byItem = new LinkedHashMap<>();

        for (Order order : orders) {
            gross = gross.add(order.totalDue());
            vat = vat.add(order.vatAmount());
            itemsSold += order.itemCount();

            for (OrderLineItem line : order.lineItems()) {
                byItem.computeIfAbsent(line.itemName(), name -> new TopSellingItemAccumulator())
                        .add(line.quantity(), line.lineTotal());
            }
        }

        List<TopSellingItem> topItems = byItem.entrySet().stream()
                .map(entry -> new TopSellingItem(entry.getKey(), entry.getValue().quantity, entry.getValue().revenue))
                .sorted(Comparator.comparingInt(TopSellingItem::quantitySold).reversed())
                .limit(TOP_SELLING_ITEMS_LIMIT)
                .toList();

        Money net = gross.subtract(vat);
        return new SalesReport(from, to, orders.size(), itemsSold, gross, vat, net, topItems);
    }

    public List<CashierSalesReport> salesByCashier(Instant from, Instant to) {
        List<Order> orders = orderRepository.findByStatusAndDateRange(OrderStatus.PAID, from, to);
        Map<Long, CashierSalesAccumulator> byCashier = new LinkedHashMap<>();

        for (Order order : orders) {
            byCashier.computeIfAbsent(order.cashierId(), id -> new CashierSalesAccumulator())
                    .add(order.totalDue());
        }

        return byCashier.entrySet().stream()
                .map(entry -> new CashierSalesReport(
                        cashierName(entry.getKey()), entry.getValue().orderCount, entry.getValue().total))
                .toList();
    }

    public List<Order> voidReport(Instant from, Instant to) {
        return orderRepository.findByStatusAndDateRange(OrderStatus.VOIDED, from, to);
    }

    private String cashierName(long cashierId) {
        return userRepository.findById(cashierId).map(User::displayName).orElse("Unknown");
    }

    private static final class TopSellingItemAccumulator {
        private int quantity;
        private Money revenue = Money.ZERO;

        void add(int additionalQuantity, Money additionalRevenue) {
            quantity += additionalQuantity;
            revenue = revenue.add(additionalRevenue);
        }
    }

    private static final class CashierSalesAccumulator {
        private int orderCount;
        private Money total = Money.ZERO;

        void add(Money orderTotal) {
            orderCount++;
            total = total.add(orderTotal);
        }
    }
}
