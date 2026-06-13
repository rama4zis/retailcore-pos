package com.retailcore.pos.user.dto;

import com.retailcore.pos.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull UserRole role,
        Boolean active
) {
}
