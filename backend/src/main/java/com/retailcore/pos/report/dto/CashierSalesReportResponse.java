package com.retailcore.pos.report.dto;

import java.math.BigDecimal;

public record CashierSalesReportResponse(
        Long cashierId,
        String cashierName,
        String cashierEmail,
        Long saleCount,
        BigDecimal totalAmount
) {
}
