package com.restaurant.pos.repository;

import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.Payment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(long id);

    CheckoutResult placeOrder(Order order, Payment payment);

    void markVoided(long orderId, long voidedByUserId, String reason, Instant voidedAt);

    List<Order> findByDateRange(Instant from, Instant to);

    List<Order> findByCashierAndDateRange(long cashierId, Instant from, Instant to);

    List<Order> findByStatusAndDateRange(OrderStatus status, Instant from, Instant to);
}
