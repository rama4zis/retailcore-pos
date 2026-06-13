package com.retailcore.pos.sale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.payment.PaymentMethod;
import com.retailcore.pos.receipt.dto.ReceiptItemResponse;
import com.retailcore.pos.receipt.dto.ReceiptPaymentResponse;
import com.retailcore.pos.receipt.dto.ReceiptResponse;
import com.retailcore.pos.sale.dto.CheckoutItemRequest;
import com.retailcore.pos.sale.dto.CheckoutPaymentRequest;
import com.retailcore.pos.sale.dto.CheckoutRequest;
import com.retailcore.pos.sale.dto.SaleItemResponse;
import com.retailcore.pos.sale.dto.SaleResponse;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = SaleController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService saleService;

    @Test
    void checkoutRequiresItems() throws Exception {
        CheckoutRequest request = new CheckoutRequest(List.of(), checkoutPayment());

        mockMvc.perform(post("/api/sales/checkout")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("items"));
    }

    @Test
    void checkoutRequiresPositiveItemQuantity() throws Exception {
        CheckoutRequest request = new CheckoutRequest(List.of(new CheckoutItemRequest(10L, 0)), checkoutPayment());

        mockMvc.perform(post("/api/sales/checkout")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("items[0].quantity"));
    }

    @Test
    void checkoutReturnsCreatedSale() throws Exception {
        when(saleService.checkout(any(String.class), any(CheckoutRequest.class))).thenReturn(receiptResponse());
        CheckoutRequest request = checkoutRequest();

        mockMvc.perform(post("/api/sales/checkout")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value(50L))
                .andExpect(jsonPath("$.saleNumber").value("SALE-001"))
                .andExpect(jsonPath("$.cashierName").value("Cashier One"))
                .andExpect(jsonPath("$.totalAmount").value(7000.00))
                .andExpect(jsonPath("$.items[0].unitPrice").value(3500.00))
                .andExpect(jsonPath("$.payment.method").value("CASH"))
                .andExpect(jsonPath("$.payment.cashTendered").value(10000.00))
                .andExpect(jsonPath("$.changeAmount").value(3000.00));

        verify(saleService).checkout(any(String.class), any(CheckoutRequest.class));
    }

    @Test
    void checkoutReturnsConflictWhenStockIsInsufficient() throws Exception {
        when(saleService.checkout(any(String.class), any(CheckoutRequest.class)))
                .thenThrow(new InsufficientStockException(10L, 2, 1));
        CheckoutRequest request = checkoutRequest();

        mockMvc.perform(post("/api/sales/checkout")
                        .principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock for product id 10: requested 2, available 1"));
    }

    @Test
    void findAllReturnsSales() throws Exception {
        when(saleService.findAll()).thenReturn(List.of(saleResponse()));

        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50L))
                .andExpect(jsonPath("$[0].items[0].sku").value("SKU-001"));
    }

    @Test
    void findByIdReturnsSale() throws Exception {
        when(saleService.findById(50L)).thenReturn(saleResponse());

        mockMvc.perform(get("/api/sales/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private static Principal principal() {
        return () -> "cashier@example.com";
    }

    private static CheckoutRequest checkoutRequest() {
        return new CheckoutRequest(
                List.of(new CheckoutItemRequest(10L, 2)),
                checkoutPayment()
        );
    }

    private static CheckoutPaymentRequest checkoutPayment() {
        return new CheckoutPaymentRequest(
                PaymentMethod.CASH,
                new BigDecimal("7000.00"),
                new BigDecimal("10000.00")
        );
    }

    private static ReceiptResponse receiptResponse() {
        return new ReceiptResponse(
                50L,
                "SALE-001",
                "Cashier One",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReceiptItemResponse(
                        10L,
                        "SKU-001",
                        "Mineral Water",
                        2,
                        new BigDecimal("3500.00"),
                        new BigDecimal("7000.00")
                )),
                new BigDecimal("7000.00"),
                new ReceiptPaymentResponse(
                        PaymentMethod.CASH,
                        new BigDecimal("7000.00"),
                        new BigDecimal("10000.00"),
                        new BigDecimal("3000.00"),
                        Instant.parse("2026-01-01T00:00:00Z")
                ),
                new BigDecimal("3000.00")
        );
    }

    private static SaleResponse saleResponse() {
        return new SaleResponse(
                50L,
                "SALE-001",
                7L,
                "Cashier One",
                SaleStatus.COMPLETED,
                new BigDecimal("7000.00"),
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new SaleItemResponse(
                        60L,
                        10L,
                        "SKU-001",
                        "Mineral Water",
                        2,
                        new BigDecimal("3500.00"),
                        new BigDecimal("7000.00")
                ))
        );
    }
}
