package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;

public interface PaymentProcessor {
    /**
     * Process a non-cash payment.
     * @param method The payment method (e.g. CARD, E_WALLET)
     * @param amount The amount to charge
     * @return A transaction reference ID if successful
     * @throws PaymentProcessingException if the payment fails
     */
    String processPayment(PaymentMethod method, Money amount) throws PaymentProcessingException;
}
