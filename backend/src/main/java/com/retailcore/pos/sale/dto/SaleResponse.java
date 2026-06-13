package com.retailcore.pos.sale.dto;

import com.retailcore.pos.sale.SaleEntity;
import com.retailcore.pos.sale.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        String saleNumber,
        Long cashierId,
        String cashierName,
        SaleStatus status,
        BigDecimal totalAmount,
        Instant completedAt,
        List<SaleItemResponse> items
) {

    public static SaleResponse from(SaleEntity sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getCashier().getId(),
                sale.getCashier().getName(),
                sale.getStatus(),
                sale.getTotalAmount(),
                sale.getCompletedAt(),
                sale.getItems().stream().map(SaleItemResponse::from).toList()
        );
    }
}
