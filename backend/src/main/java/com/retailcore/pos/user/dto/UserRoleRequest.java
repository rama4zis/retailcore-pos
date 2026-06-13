package com.retailcore.pos.user.dto;

import com.retailcore.pos.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(@NotNull UserRole role) {
}
