package com.retailcore.pos.payment;

import java.math.BigDecimal;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(BigDecimal amount, BigDecimal saleTotal) {
        super("Payment amount " + amount + " must equal sale total " + saleTotal);
    }
}
