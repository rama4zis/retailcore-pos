package com.retailcore.pos.refund.dto;

import com.retailcore.pos.refund.RefundEntity;
import com.retailcore.pos.sale.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RefundResponse(
        Long id,
        Long saleId,
        String saleNumber,
        SaleStatus saleStatus,
        BigDecimal totalAmount,
        String reason,
        Instant refundedAt,
        List<RefundItemResponse> items
) {

    public static RefundResponse from(RefundEntity refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getSale().getId(),
                refund.getSale().getSaleNumber(),
                refund.getSale().getStatus(),
                refund.getTotalAmount(),
                refund.getReason(),
                refund.getRefundedAt(),
                refund.getItems().stream().map(RefundItemResponse::from).toList()
        );
    }
}
