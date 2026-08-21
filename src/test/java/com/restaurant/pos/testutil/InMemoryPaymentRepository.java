package com.restaurant.pos.testutil;

import com.restaurant.pos.model.Payment;
import com.restaurant.pos.repository.PaymentRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> paymentsByOrderId = new LinkedHashMap<>();

    @Override
    public Optional<Payment> findByOrderId(long orderId) {
        return Optional.ofNullable(paymentsByOrderId.get(orderId));
    }

    public void put(Payment payment) {
        paymentsByOrderId.put(payment.orderId(), payment);
    }
}
