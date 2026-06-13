package com.retailcore.pos.sale.dto;

import com.retailcore.pos.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CheckoutPaymentRequest(
        @NotNull PaymentMethod method,
        @NotNull @PositiveOrZero BigDecimal amount,
        @PositiveOrZero BigDecimal cashTendered
) {
}
