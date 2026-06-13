package com.retailcore.pos.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotNull(message = "Category id is required")
        Long categoryId,

        @NotBlank(message = "SKU is required")
        @Size(max = 80, message = "SKU must be at most 80 characters")
        String sku,

        @Size(max = 80, message = "Barcode must be at most 80 characters")
        String barcode,

        @NotBlank(message = "ProductEntity name is required")
        @Size(max = 160, message = "ProductEntity name must be at most 160 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        Boolean active
) {
    public boolean activeOrDefault() {
        return active == null || active;
    }
}
