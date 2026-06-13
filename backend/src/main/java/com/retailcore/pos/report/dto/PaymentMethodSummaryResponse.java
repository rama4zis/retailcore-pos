package com.retailcore.pos.report.dto;

import com.retailcore.pos.payment.PaymentMethod;
import java.math.BigDecimal;

public record PaymentMethodSummaryResponse(
        PaymentMethod method,
        Long paymentCount,
        BigDecimal totalAmount
) {
}
