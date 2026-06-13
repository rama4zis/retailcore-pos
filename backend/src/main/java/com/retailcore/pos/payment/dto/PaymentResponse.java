package com.retailcore.pos.payment.dto;

import com.retailcore.pos.payment.PaymentEntity;
import com.retailcore.pos.payment.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long saleId,
        String saleNumber,
        PaymentMethod method,
        BigDecimal amount,
        BigDecimal cashTendered,
        BigDecimal changeAmount,
        Instant paidAt
) {

    public static PaymentResponse from(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getSale().getId(),
                payment.getSale().getSaleNumber(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getCashTendered(),
                payment.getChangeAmount(),
                payment.getPaidAt()
        );
    }
}
