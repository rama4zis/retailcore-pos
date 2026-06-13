package com.retailcore.pos.sale.dto;

import com.retailcore.pos.sale.SaleItemEntity;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sale line item response with price snapshot data.")
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

