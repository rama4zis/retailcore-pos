package com.retailcore.pos.report.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top-selling product report response.")
public record TopSellingProductResponse(
        Long productId,
        String sku,
        String productName,
        Long quantitySold,
        BigDecimal grossSales
) {
}

