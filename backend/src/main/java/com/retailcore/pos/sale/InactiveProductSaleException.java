package com.retailcore.pos.sale;

public class InactiveProductSaleException extends RuntimeException {

    public InactiveProductSaleException(Long productId) {
        super("Cannot sell inactive product with id: " + productId);
    }
}
