package com.retailcore.pos.refund.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refund line item request.")
public record RefundItemRequest(
        @NotNull(message = "Product id is required")
        Long productId,

        @Positive(message = "Quantity must be positive")
        int quantity
) {
}

