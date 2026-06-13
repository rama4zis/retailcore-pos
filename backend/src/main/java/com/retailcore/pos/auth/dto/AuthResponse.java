package com.retailcore.pos.auth.dto;

import com.retailcore.pos.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
