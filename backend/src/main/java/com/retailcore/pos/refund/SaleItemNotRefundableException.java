package com.retailcore.pos.refund;

public class SaleItemNotRefundableException extends RuntimeException {

    public SaleItemNotRefundableException(Long saleId, Long productId) {
        super("Product id " + productId + " is not part of sale id " + saleId);
    }
}
