package com.retailcore.pos.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to adjust product stock and optionally update the low-stock threshold.")
public record StockAdjustmentRequest(
        @NotNull(message = "Quantity change is required")
        Integer quantityChange,

        @Min(value = 0, message = "Low stock threshold cannot be negative")
        Integer lowStockThreshold,

        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}
