package com.retailcore.pos.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createRejectsNonPositivePrice() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                BigDecimal.ZERO,
                true
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("price"));
    }

    @Test
    void createReturnsCreatedProductLocation() throws Exception {
        ProductResponse response = new ProductResponse(
                10L,
                1L,
                "Beverages",
                "SKU-001",
                "BAR-001",
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(productService.create(any(ProductCreateRequest.class))).thenReturn(response);

        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "SKU-001",
                "BAR-001",
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.sku").value("SKU-001"));

        verify(productService).create(any(ProductCreateRequest.class));
    }

    @Test
    void changeActiveRequiresActiveFlag() throws Exception {
        mockMvc.perform(patch("/api/products/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("active"));
    }
}
