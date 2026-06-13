package com.retailcore.pos.product.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to enable or disable a product.")
public record ProductActiveRequest(
        @NotNull(message = "Active flag is required")
        Boolean active
) {
}
