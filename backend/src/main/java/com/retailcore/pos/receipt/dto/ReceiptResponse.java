package com.retailcore.pos.receipt.dto;

import com.retailcore.pos.payment.PaymentEntity;
import com.retailcore.pos.sale.SaleEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Receipt response returned after checkout.")
public record ReceiptResponse(
        Long saleId,
        String saleNumber,
        String cashierName,
        Instant completedAt,
        List<ReceiptItemResponse> items,
        BigDecimal totalAmount,
        ReceiptPaymentResponse payment,
        BigDecimal changeAmount
) {

    public static ReceiptResponse from(SaleEntity sale, PaymentEntity payment) {
        return new ReceiptResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getCashier().getName(),
                sale.getCompletedAt(),
                sale.getItems().stream().map(ReceiptItemResponse::from).toList(),
                sale.getTotalAmount(),
                ReceiptPaymentResponse.from(payment),
                payment.getChangeAmount()
        );
    }
}

