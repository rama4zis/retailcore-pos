package com.retailcore.pos.payment;

import com.retailcore.pos.common.exception.DuplicateResourceException;

public class DuplicatePaymentException extends DuplicateResourceException {

    public DuplicatePaymentException(Long saleId) {
        super("Payment already exists for sale id: " + saleId);
    }
}
