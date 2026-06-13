package com.retailcore.pos.report;

import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.dto.InventoryStockResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryStockRepository inventoryStockRepository;

    @Transactional(readOnly = true)
    public SalesTotalResponse dailySales(LocalDate date) {
        BigDecimal total = saleRepository.sumTotalAmountByCompletedAtBetween(
                date.atStartOfDay().toInstant(ZoneOffset.UTC),
                date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );
        return new SalesTotalResponse(date.toString(), zeroWhenNull(total));
    }

    @Transactional(readOnly = true)
    public SalesTotalResponse monthlySales(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        BigDecimal total = saleRepository.sumTotalAmountByCompletedAtBetween(
                yearMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );
        return new SalesTotalResponse(yearMonth.toString(), zeroWhenNull(total));
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> topProducts() {
        return saleItemRepository.findTopSellingProducts();
    }

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> lowStock() {
        return inventoryStockRepository.findLowStock();
    }

    @Transactional(readOnly = true)
    public List<CashierSalesReportResponse> salesByCashier() {
        return saleRepository.findSalesByCashier();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodSummaryResponse> paymentSummary() {
        return paymentRepository.summarizeByPaymentMethod();
    }

    private static BigDecimal zeroWhenNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
