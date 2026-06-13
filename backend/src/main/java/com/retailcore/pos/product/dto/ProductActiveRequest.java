package com.retailcore.pos.product.dto;

import jakarta.validation.constraints.NotNull;

public record ProductActiveRequest(
        @NotNull(message = "Active flag is required")
        Boolean active
) {
}
