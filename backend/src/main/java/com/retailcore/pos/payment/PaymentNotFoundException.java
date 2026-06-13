package com.retailcore.pos.payment;

import com.retailcore.pos.common.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }
}
