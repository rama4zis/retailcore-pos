package com.retailcore.pos.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Checkout request containing sale items and payment details.")
public record CheckoutRequest(
        @NotEmpty List<@Valid CheckoutItemRequest> items,
        @NotNull @Valid CheckoutPaymentRequest payment
) {
}

