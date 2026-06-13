package com.retailcore.pos.product.exception;

public class ProductNotFoundException extends com.retailcore.pos.common.exception.ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
