package com.restaurant.pos.testutil;

import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.repository.OrderRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory {@link OrderRepository} fake for service-layer unit tests. */
public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> ordersById = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public Optional<Order> findById(long id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    @Override
    public CheckoutResult placeOrder(Order order, Payment payment) {
        long orderId = idSequence.incrementAndGet();
        Order saved = order.toBuilder().id(orderId).build();
        ordersById.put(orderId, saved);
        Payment savedPayment = payment.toBuilder().id(orderId).orderId(orderId).build();
        return new CheckoutResult(saved, savedPayment);
    }

    @Override
    public void markVoided(long orderId, long voidedByUserId, String reason, Instant voidedAt) {
        Order existing = ordersById.get(orderId);
        ordersById.put(orderId, existing.toBuilder()
                .status(OrderStatus.VOIDED)
                .voidedAt(voidedAt)
                .voidedByUserId(voidedByUserId)
                .voidReason(reason)
                .build());
    }

    @Override
    public List<Order> findByDateRange(Instant from, Instant to) {
        return ordersById.values().stream()
                .filter(o -> !o.placedAt().isBefore(from) && o.placedAt().isBefore(to))
                .toList();
    }

    @Override
    public List<Order> findByCashierAndDateRange(long cashierId, Instant from, Instant to) {
        return findByDateRange(from, to).stream().filter(o -> o.cashierId() == cashierId).toList();
    }

    @Override
    public List<Order> findByStatusAndDateRange(OrderStatus status, Instant from, Instant to) {
        return findByDateRange(from, to).stream().filter(o -> o.status() == status).toList();
    }
}
