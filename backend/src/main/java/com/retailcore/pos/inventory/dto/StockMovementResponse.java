package com.retailcore.pos.inventory.dto;

import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementType;
import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long productId,
        String sku,
        StockMovementType movementType,
        int quantityChange,
        int stockAfter,
        String reason,
        Instant createdAt
) {
    public static StockMovementResponse from(StockMovementEntity movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getMovementType(),
                movement.getQuantityChange(),
                movement.getStockAfter(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}
