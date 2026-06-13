package com.retailcore.pos.product.exception;

public class DuplicateProductBarcodeException extends com.retailcore.pos.common.exception.DuplicateResourceException {

    public DuplicateProductBarcodeException(String barcode) {
        super("Product barcode already exists: " + barcode);
    }
}
