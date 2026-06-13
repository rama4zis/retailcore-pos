package com.retailcore.pos.auth.dto;

import com.retailcore.pos.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT login response with the authenticated user profile.")
public record AuthResponse(String token, UserResponse user) {
}

