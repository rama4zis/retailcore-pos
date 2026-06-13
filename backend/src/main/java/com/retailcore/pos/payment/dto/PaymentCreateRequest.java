package com.retailcore.pos.payment.dto;

import com.retailcore.pos.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create a cash or card payment for a sale.")
public record PaymentCreateRequest(
        @NotNull Long saleId,
        @NotNull PaymentMethod method,
        @NotNull @PositiveOrZero BigDecimal amount,
        @PositiveOrZero BigDecimal cashTendered
) {
}

