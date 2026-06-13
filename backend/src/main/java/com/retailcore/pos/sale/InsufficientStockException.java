package com.retailcore.pos.sale;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for product id " + productId
                + ": requested " + requestedQuantity + ", available " + availableQuantity);
    }
}
