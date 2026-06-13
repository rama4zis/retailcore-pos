package com.retailcore.pos.refund.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to refund one or more sale item quantities.")
public record RefundRequest(
        @NotEmpty(message = "Refund items are required")
        List<@Valid RefundItemRequest> items,

        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}

