package com.retailcore.pos.report;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.retailcore.pos.auth.JwtService;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.config.SecurityConfig;
import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.payment.PaymentMethod;
import com.retailcore.pos.report.dto.CashierSalesReportResponse;
import com.retailcore.pos.report.dto.PaymentMethodSummaryResponse;
import com.retailcore.pos.report.dto.SalesTotalResponse;
import com.retailcore.pos.report.dto.TopSellingProductResponse;
import com.retailcore.pos.user.UserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void reportsRequireManagerOrAdminRole() throws Exception {
        mockMvc.perform(get("/api/reports/daily-sales")
                        .param("date", "2026-01-15")
                        .with(user("cashier").roles("CASHIER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void dailySalesReturnsReportForRequestedDate() throws Exception {
        when(reportService.dailySales(LocalDate.of(2026, 1, 15)))
                .thenReturn(new SalesTotalResponse("2026-01-15", new BigDecimal("125000.00")));

        mockMvc.perform(get("/api/reports/daily-sales")
                        .param("date", "2026-01-15")
                        .with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("2026-01-15"))
                .andExpect(jsonPath("$.totalAmount").value(125000.00));
    }

    @Test
    void monthlySalesReturnsReportForRequestedMonth() throws Exception {
        when(reportService.monthlySales(2026, 1))
                .thenReturn(new SalesTotalResponse("2026-01", new BigDecimal("450000.00")));

        mockMvc.perform(get("/api/reports/monthly-sales")
                        .param("year", "2026")
                        .param("month", "1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("2026-01"))
                .andExpect(jsonPath("$.totalAmount").value(450000.00));
    }

    @Test
    void topProductsReturnsRankedProducts() throws Exception {
        when(reportService.topProducts()).thenReturn(List.of(
                new TopSellingProductResponse(10L, "SKU-001", "Mineral Water", 12L, new BigDecimal("60000.00"))
        ));

        mockMvc.perform(get("/api/reports/top-products").with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].quantitySold").value(12))
                .andExpect(jsonPath("$[0].grossSales").value(60000.00));
    }

    @Test
    void lowStockReturnsLowStockProducts() throws Exception {
        when(reportService.lowStock()).thenReturn(List.of(stockResponse()));

        mockMvc.perform(get("/api/reports/low-stock").with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void salesByCashierReturnsCashierTotals() throws Exception {
        when(reportService.salesByCashier()).thenReturn(List.of(
                new CashierSalesReportResponse(5L, "Cashier One", "cashier@example.com", 3L, new BigDecimal("95000.00"))
        ));

        mockMvc.perform(get("/api/reports/sales-by-cashier").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cashierId").value(5L))
                .andExpect(jsonPath("$[0].saleCount").value(3))
                .andExpect(jsonPath("$[0].totalAmount").value(95000.00));
    }

    @Test
    void paymentSummaryReturnsPaymentMethodTotals() throws Exception {
        when(reportService.paymentSummary()).thenReturn(List.of(
                new PaymentMethodSummaryResponse(PaymentMethod.CASH, 4L, new BigDecimal("120000.00"))
        ));

        mockMvc.perform(get("/api/reports/payment-summary").with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].method").value("CASH"))
                .andExpect(jsonPath("$[0].paymentCount").value(4))
                .andExpect(jsonPath("$[0].totalAmount").value(120000.00));
    }

    private static InventoryStockResponse stockResponse() {
        return new InventoryStockResponse(
                10L,
                "SKU-001",
                "Mineral Water",
                1,
                2,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
