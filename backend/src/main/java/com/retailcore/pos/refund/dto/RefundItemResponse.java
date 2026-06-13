package com.retailcore.pos.refund.dto;

import com.retailcore.pos.refund.RefundItemEntity;
import java.math.BigDecimal;

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
