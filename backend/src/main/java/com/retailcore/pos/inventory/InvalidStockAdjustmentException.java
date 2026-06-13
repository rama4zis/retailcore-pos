package com.retailcore.pos.inventory;

public class InvalidStockAdjustmentException extends RuntimeException {

    public InvalidStockAdjustmentException(String message) {
        super(message);
    }
}
