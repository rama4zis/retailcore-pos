package com.retailcore.pos.report;

import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.retailcore.pos.report.dto.CashierSalesReportResponse;
import com.retailcore.pos.report.dto.PaymentMethodSummaryResponse;
import com.retailcore.pos.report.dto.SalesTotalResponse;
import com.retailcore.pos.report.dto.TopSellingProductResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Reports", description = "Manager and admin reporting dashboard endpoints")
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Daily sales total", description = "Returns total sales for one calendar date.")
    @GetMapping("/daily-sales")
    public ResponseEntity<SalesTotalResponse> dailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(reportService.dailySales(date));
    }

    @Operation(summary = "Monthly sales total", description = "Returns total sales for one calendar month.")
    @GetMapping("/monthly-sales")
    public ResponseEntity<SalesTotalResponse> monthlySales(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(reportService.monthlySales(year, month));
    }

    @Operation(summary = "Top-selling products", description = "Ranks products by sold quantity and gross sales.")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopSellingProductResponse>> topProducts() {
        return ResponseEntity.ok(reportService.topProducts());
    }

    @Operation(summary = "Low-stock report", description = "Returns low-stock products for reporting dashboards.")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryStockResponse>> lowStock() {
        return ResponseEntity.ok(reportService.lowStock());
    }

    @Operation(summary = "Sales by cashier", description = "Groups sales totals by cashier.")
    @GetMapping("/sales-by-cashier")
    public ResponseEntity<List<CashierSalesReportResponse>> salesByCashier() {
        return ResponseEntity.ok(reportService.salesByCashier());
    }

    @Operation(summary = "Payment method summary", description = "Groups payment totals by payment method.")
    @GetMapping("/payment-summary")
    public ResponseEntity<List<PaymentMethodSummaryResponse>> paymentSummary() {
        return ResponseEntity.ok(reportService.paymentSummary());
    }
}

