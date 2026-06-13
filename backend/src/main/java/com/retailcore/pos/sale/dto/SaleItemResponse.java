package com.retailcore.pos.sale.dto;

import com.retailcore.pos.sale.SaleItemEntity;
import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static SaleItemResponse from(SaleItemEntity item) {
        return new SaleItemResponse(
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
