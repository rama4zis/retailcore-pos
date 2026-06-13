package com.retailcore.pos.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
