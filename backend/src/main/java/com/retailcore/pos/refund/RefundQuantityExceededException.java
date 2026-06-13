package com.retailcore.pos.refund;

public class RefundQuantityExceededException extends RuntimeException {

    public RefundQuantityExceededException(Long productId, int requested, int refundable) {
        super("Refund quantity for product id " + productId + " exceeds sold quantity: requested "
                + requested + ", refundable " + refundable);
    }
}
