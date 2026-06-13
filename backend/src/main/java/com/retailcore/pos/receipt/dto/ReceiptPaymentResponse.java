package com.retailcore.pos.receipt.dto;

import com.retailcore.pos.payment.PaymentEntity;
import com.retailcore.pos.payment.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptPaymentResponse(
        PaymentMethod method,
        BigDecimal amount,
        BigDecimal cashTendered,
        BigDecimal changeAmount,
        Instant paidAt
) {

    public static ReceiptPaymentResponse from(PaymentEntity payment) {
        return new ReceiptPaymentResponse(
                payment.getMethod(),
                payment.getAmount(),
                payment.getCashTendered(),
                payment.getChangeAmount(),
                payment.getPaidAt()
        );
    }
}
