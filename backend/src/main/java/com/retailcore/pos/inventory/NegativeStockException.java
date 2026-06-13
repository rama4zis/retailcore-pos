package com.retailcore.pos.inventory;

public class NegativeStockException extends RuntimeException {

    public NegativeStockException(Long productId, int attemptedQuantity) {
        super("Stock cannot become negative for product id " + productId + ": attempted quantity " + attemptedQuantity);
    }
}
