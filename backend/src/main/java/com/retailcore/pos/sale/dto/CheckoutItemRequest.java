package com.retailcore.pos.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Checkout line item request.")
public record CheckoutItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}

