package com.retailcore.pos.report.dto;

import java.math.BigDecimal;

public record TopSellingProductResponse(
        Long productId,
        String sku,
        String productName,
        Long quantitySold,
        BigDecimal grossSales
) {
}
