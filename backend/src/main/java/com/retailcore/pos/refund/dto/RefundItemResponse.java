package com.retailcore.pos.refund.dto;

import com.retailcore.pos.refund.RefundItemEntity;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refund line item response.")
public record RefundItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static RefundItemResponse from(RefundItemEntity item) {
        return new RefundItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}

