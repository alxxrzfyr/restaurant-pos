package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;

import java.util.UUID;

public class MockPaymentProcessor implements PaymentProcessor {
    @Override
    public String processPayment(PaymentMethod method, Money amount) throws PaymentProcessingException {

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment interrupted");
        }

        return "MOCK-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
