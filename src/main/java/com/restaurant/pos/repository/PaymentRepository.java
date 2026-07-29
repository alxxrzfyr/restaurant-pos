package com.restaurant.pos.repository;

import com.restaurant.pos.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findByOrderId(long orderId);
}
