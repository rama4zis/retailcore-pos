package com.retailcore.pos.sale;

import com.retailcore.pos.receipt.dto.ReceiptResponse;
import com.retailcore.pos.refund.RefundService;
import com.retailcore.pos.refund.dto.RefundRequest;
import com.retailcore.pos.refund.dto.RefundResponse;
import com.retailcore.pos.sale.dto.CheckoutRequest;
import com.retailcore.pos.sale.dto.SaleResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
public class SaleController {

    private final SaleService saleService;
    private final RefundService refundService;

    @PostMapping("/checkout")
    public ResponseEntity<ReceiptResponse> checkout(
            Principal principal,
            @Valid @RequestBody CheckoutRequest request
    ) {
        ReceiptResponse response = saleService.checkout(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/sales/" + response.saleId())).body(response);
    }

    @PostMapping("/{id}/refunds")
    public ResponseEntity<RefundResponse> refund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request
    ) {
        RefundResponse response = refundService.refund(id, request);
        return ResponseEntity.created(URI.create("/api/sales/" + id + "/refunds/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> findAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }
}
