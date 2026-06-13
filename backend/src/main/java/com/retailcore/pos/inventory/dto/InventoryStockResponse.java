package com.retailcore.pos.inventory.dto;

import com.retailcore.pos.inventory.InventoryStockEntity;
import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product inventory stock response.")
public record InventoryStockResponse(
        Long productId,
        String sku,
        String productName,
        int quantity,
        int lowStockThreshold,
        boolean lowStock,
        Instant createdAt,
        Instant updatedAt
) {
    public static InventoryStockResponse from(InventoryStockEntity stock) {
        return new InventoryStockResponse(
                stock.getProduct().getId(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getLowStockThreshold(),
                stock.getQuantity() <= stock.getLowStockThreshold(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }
}
