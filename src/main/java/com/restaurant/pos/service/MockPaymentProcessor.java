package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;

import java.util.UUID;

public class MockPaymentProcessor implements PaymentProcessor {
    @Override
    public String processPayment(PaymentMethod method, Money amount) throws PaymentProcessingException {
        // Simulate network delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment interrupted");
        }
        
        // In a real app, this would integrate with a payment gateway (e.g. Stripe, Maya, GCash API).
        // Since this is a mock layer, we just return a fake transaction ID.
        return "MOCK-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
