package com.retailcore.pos.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.auth.JwtService;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.config.SecurityConfig;
import com.retailcore.pos.payment.dto.PaymentCreateRequest;
import com.retailcore.pos.payment.dto.PaymentResponse;
import com.retailcore.pos.user.UserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void createRequiresAuthorizedRole() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .with(user("viewer").roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReturnsCreatedPayment() throws Exception {
        when(paymentService.create(any(PaymentCreateRequest.class))).thenReturn(response(PaymentMethod.CASH));

        mockMvc.perform(post("/api/payments")
                        .with(user("cashier").roles("CASHIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/payments/80"))
                .andExpect(jsonPath("$.saleId").value(50L))
                .andExpect(jsonPath("$.method").value("CASH"))
                .andExpect(jsonPath("$.amount").value(7000.00))
                .andExpect(jsonPath("$.cashTendered").value(10000.00))
                .andExpect(jsonPath("$.changeAmount").value(3000.00));

        verify(paymentService).create(any(PaymentCreateRequest.class));
    }

    @Test
    void createRequiresPaymentMethod() throws Exception {
        PaymentCreateRequest request = new PaymentCreateRequest(50L, null, new BigDecimal("7000.00"), null);

        mockMvc.perform(post("/api/payments")
                        .with(user("cashier").roles("CASHIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("method"));
    }

    @Test
    void createReturnsConflictForAmountMismatch() throws Exception {
        when(paymentService.create(any(PaymentCreateRequest.class)))
                .thenThrow(new PaymentAmountMismatchException(new BigDecimal("6000.00"), new BigDecimal("7000.00")));

        mockMvc.perform(post("/api/payments")
                        .with(user("cashier").roles("CASHIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Payment amount 6000.00 must equal sale total 7000.00"));
    }

    @Test
    void findAllReturnsPayments() throws Exception {
        when(paymentService.findAll()).thenReturn(List.of(response(PaymentMethod.CARD)));

        mockMvc.perform(get("/api/payments").with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].method").value("CARD"));
    }

    @Test
    void findByIdReturnsPayment() throws Exception {
        when(paymentService.findById(80L)).thenReturn(response(PaymentMethod.CASH));

        mockMvc.perform(get("/api/payments/80").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(80L));
    }

    private static PaymentCreateRequest cashRequest() {
        return new PaymentCreateRequest(
                50L,
                PaymentMethod.CASH,
                new BigDecimal("7000.00"),
                new BigDecimal("10000.00")
        );
    }

    private static PaymentResponse response(PaymentMethod method) {
        return new PaymentResponse(
                80L,
                50L,
                "SALE-001",
                method,
                new BigDecimal("7000.00"),
                method == PaymentMethod.CASH ? new BigDecimal("10000.00") : null,
                method == PaymentMethod.CASH ? new BigDecimal("3000.00") : BigDecimal.ZERO,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
