package com.retailcore.pos.user.dto;

import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRole;
import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User account response returned by authentication and user management endpoints.")
public record UserResponse(
        Long id,
        String email,
        String name,
        UserRole role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

