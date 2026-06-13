package com.retailcore.pos.receipt.dto;

import com.retailcore.pos.sale.SaleItemEntity;
import java.math.BigDecimal;

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
