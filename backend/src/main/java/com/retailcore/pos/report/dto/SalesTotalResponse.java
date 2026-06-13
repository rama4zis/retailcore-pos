package com.retailcore.pos.report.dto;

import java.math.BigDecimal;

public record SalesTotalResponse(
        String period,
        BigDecimal totalAmount
) {
}
