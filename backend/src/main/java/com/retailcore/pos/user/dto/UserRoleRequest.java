package com.retailcore.pos.user.dto;

import com.retailcore.pos.user.UserRole;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to change a user role.")
public record UserRoleRequest(@NotNull UserRole role) {
}

