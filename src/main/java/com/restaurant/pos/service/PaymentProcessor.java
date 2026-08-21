package com.restaurant.pos.service;

import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.PaymentMethod;

public interface PaymentProcessor {

    String processPayment(PaymentMethod method, Money amount) throws PaymentProcessingException;
}
