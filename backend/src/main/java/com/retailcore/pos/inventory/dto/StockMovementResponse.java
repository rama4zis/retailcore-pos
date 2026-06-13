package com.retailcore.pos.inventory.dto;

import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementType;
import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stock movement history response.")
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
