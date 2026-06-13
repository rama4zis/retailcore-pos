package com.retailcore.pos.receipt.dto;

import com.retailcore.pos.sale.SaleItemEntity;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Receipt line item response with sale-time product snapshot data.")
public record ReceiptItemResponse(
        Long productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static ReceiptItemResponse from(SaleItemEntity item) {
        return new ReceiptItemResponse(
                item.getProduct().getId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}

