package com.retailcore.pos.refund;

public class RefundAmountExceededException extends RuntimeException {

    public RefundAmountExceededException() {
        super("Refund amount must not exceed original sale amount");
    }
}
