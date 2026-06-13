package com.retailcore.pos.payment;

import java.math.BigDecimal;

public class InsufficientCashTenderedException extends RuntimeException {

    public InsufficientCashTenderedException() {
        super("Cash tendered is required for cash payments");
    }

    public InsufficientCashTenderedException(BigDecimal cashTendered, BigDecimal amount) {
        super("Cash tendered " + cashTendered + " is less than payment amount " + amount);
    }
}
