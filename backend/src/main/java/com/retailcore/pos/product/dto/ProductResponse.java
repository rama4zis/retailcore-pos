package com.retailcore.pos.product.dto;

import com.retailcore.pos.product.ProductEntity;
import java.math.BigDecimal;
import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product catalog item response.")
public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String sku,
        String barcode,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
