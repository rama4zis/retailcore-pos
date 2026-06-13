package com.retailcore.pos.report.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sales total report response for a date or month.")
public record SalesTotalResponse(
        String period,
        BigDecimal totalAmount
) {
}

