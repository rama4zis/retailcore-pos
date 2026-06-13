package com.retailcore.pos.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.payment.PaymentMethod;
import com.retailcore.pos.payment.PaymentRepository;
import com.retailcore.pos.report.dto.CashierSalesReportResponse;
import com.retailcore.pos.report.dto.PaymentMethodSummaryResponse;
import com.retailcore.pos.report.dto.SalesTotalResponse;
import com.retailcore.pos.report.dto.TopSellingProductResponse;
import com.retailcore.pos.sale.SaleItemRepository;
import com.retailcore.pos.sale.SaleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void dailySalesReturnsRequestedDateTotal() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        when(saleRepository.sumTotalAmountByCompletedAtBetween(date.atStartOfDay().toInstant(ZoneOffset.UTC), date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(new BigDecimal("125000.00"));

        SalesTotalResponse response = reportService.dailySales(date);

        assertThat(response.period()).isEqualTo("2026-01-15");
        assertThat(response.totalAmount()).isEqualByComparingTo("125000.00");
    }

    @Test
    void dailySalesReturnsZeroWhenNoSalesExist() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        when(saleRepository.sumTotalAmountByCompletedAtBetween(date.atStartOfDay().toInstant(ZoneOffset.UTC), date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(null);

        SalesTotalResponse response = reportService.dailySales(date);

        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void monthlySalesReturnsRequestedMonthTotal() {
        YearMonth month = YearMonth.of(2026, 1);
        when(saleRepository.sumTotalAmountByCompletedAtBetween(month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC), month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .thenReturn(new BigDecimal("450000.00"));

        SalesTotalResponse response = reportService.monthlySales(2026, 1);

        assertThat(response.period()).isEqualTo("2026-01");
        assertThat(response.totalAmount()).isEqualByComparingTo("450000.00");
    }

    @Test
    void topProductsDelegatesToSaleItemReportQuery() {
        List<TopSellingProductResponse> expected = List.of(
                new TopSellingProductResponse(10L, "SKU-001", "Mineral Water", 12L, new BigDecimal("60000.00"))
        );
        when(saleItemRepository.findTopSellingProducts()).thenReturn(expected);

        List<TopSellingProductResponse> response = reportService.topProducts();

        assertThat(response).containsExactlyElementsOf(expected);
    }

    @Test
    void lowStockDelegatesToInventoryReportQuery() {
        List<InventoryStockResponse> expected = List.of(stockResponse());
        when(inventoryStockRepository.findLowStock()).thenReturn(expected);

        List<InventoryStockResponse> response = reportService.lowStock();

        assertThat(response).containsExactlyElementsOf(expected);
    }

    @Test
    void salesByCashierDelegatesToSalesReportQuery() {
        List<CashierSalesReportResponse> expected = List.of(
                new CashierSalesReportResponse(5L, "Cashier One", "cashier@example.com", 3L, new BigDecimal("95000.00"))
        );
        when(saleRepository.findSalesByCashier()).thenReturn(expected);

        List<CashierSalesReportResponse> response = reportService.salesByCashier();

        assertThat(response).containsExactlyElementsOf(expected);
    }

    @Test
    void paymentSummaryDelegatesToPaymentReportQuery() {
        List<PaymentMethodSummaryResponse> expected = List.of(
                new PaymentMethodSummaryResponse(PaymentMethod.CASH, 4L, new BigDecimal("120000.00"))
        );
        when(paymentRepository.summarizeByPaymentMethod()).thenReturn(expected);

        List<PaymentMethodSummaryResponse> response = reportService.paymentSummary();

        assertThat(response).containsExactlyElementsOf(expected);
    }

    private static InventoryStockResponse stockResponse() {
        return new InventoryStockResponse(
                10L,
                "SKU-001",
                "Mineral Water",
                1,
                2,
                true,
                java.time.Instant.parse("2026-01-01T00:00:00Z"),
                java.time.Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
