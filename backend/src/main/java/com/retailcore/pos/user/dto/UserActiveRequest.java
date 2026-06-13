package com.retailcore.pos.user.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to enable or disable a user account.")
public record UserActiveRequest(@NotNull Boolean active) {
}

