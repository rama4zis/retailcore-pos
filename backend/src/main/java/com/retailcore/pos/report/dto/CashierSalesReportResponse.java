package com.retailcore.pos.report.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sales totals grouped by cashier.")
public record CashierSalesReportResponse(
        Long cashierId,
        String cashierName,
        String cashierEmail,
        Long saleCount,
        BigDecimal totalAmount
) {
}

