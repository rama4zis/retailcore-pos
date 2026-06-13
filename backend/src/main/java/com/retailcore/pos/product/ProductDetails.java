package com.retailcore.pos.product;

import com.retailcore.pos.category.CategoryEntity;
import java.math.BigDecimal;

public record ProductDetails(
        CategoryEntity category,
        String sku,
        String barcode,
        String name,
        String description,
        BigDecimal price,
        boolean active
) {
    public ProductDetails {
        sku = normalizeRequired(sku);
        barcode = normalizeOptional(barcode);
        name = normalizeRequired(name);
        description = normalizeOptional(description);
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
