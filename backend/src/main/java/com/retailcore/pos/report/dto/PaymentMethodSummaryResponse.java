package com.retailcore.pos.report.dto;

import com.retailcore.pos.payment.PaymentMethod;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment totals grouped by payment method.")
public record PaymentMethodSummaryResponse(
        PaymentMethod method,
        Long paymentCount,
        BigDecimal totalAmount
) {
}

