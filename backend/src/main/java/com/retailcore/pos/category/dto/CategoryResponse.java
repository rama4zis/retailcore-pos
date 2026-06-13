package com.retailcore.pos.category.dto;

import com.retailcore.pos.category.CategoryEntity;
import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
