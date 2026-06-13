package com.retailcore.pos.sale.dto;

import com.retailcore.pos.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment details supplied during checkout.")
public record CheckoutPaymentRequest(
        @NotNull PaymentMethod method,
        @NotNull @PositiveOrZero BigDecimal amount,
        @PositiveOrZero BigDecimal cashTendered
) {
}

