package com.retailcore.pos.product.exception;

public class DuplicateProductSkuException extends com.retailcore.pos.common.exception.DuplicateResourceException {

    public DuplicateProductSkuException(String sku) {
        super("Product SKU already exists: " + sku);
    }
}
