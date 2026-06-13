package com.retailcore.pos.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;
import com.retailcore.pos.product.dto.ProductUpdateRequest;
import java.math.BigDecimal;
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
        ProductResponse response = response(10L, 1L, "Beverages", "SKU-001", "BAR-001", true);
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
                .andExpect(header().string("Location", "/api/products/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.sku").value("SKU-001"));

        verify(productService).create(any(ProductCreateRequest.class));
    }

    @Test
    void findAllReturnsProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(
                response(10L, 1L, "Beverages", "SKU-001", "BAR-001", true),
                response(11L, 2L, "Snacks", "SKU-002", null, false)
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].categoryName").value("Beverages"))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    void findByIdReturnsProduct() throws Exception {
        when(productService.findById(10L)).thenReturn(
                response(10L, 1L, "Beverages", "SKU-001", "BAR-001", true)
        );

        mockMvc.perform(get("/api/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void updateRequiresActiveFlag() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest(
                1L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                null
        );

        mockMvc.perform(put("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("active"));
    }

    @Test
    void updateReturnsProduct() throws Exception {
        ProductResponse response = response(10L, 2L, "Snacks", "SKU-002", null, false);
        when(productService.update(any(Long.class), any(ProductUpdateRequest.class))).thenReturn(response);

        ProductUpdateRequest request = new ProductUpdateRequest(
                2L,
                "SKU-002",
                null,
                "Potato Chips",
                "Crispy snack",
                new BigDecimal("12000.00"),
                false
        );

        mockMvc.perform(put("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.categoryName").value("Snacks"))
                .andExpect(jsonPath("$.sku").value("SKU-002"))
                .andExpect(jsonPath("$.active").value(false));

        verify(productService).update(any(Long.class), any(ProductUpdateRequest.class));
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

    @Test
    void changeActiveReturnsProduct() throws Exception {
        ProductResponse response = response(10L, 1L, "Beverages", "SKU-001", "BAR-001", false);
        when(productService.changeActive(any(Long.class), any())).thenReturn(response);

        mockMvc.perform(patch("/api/products/10/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.active").value(false));

        verify(productService).changeActive(any(Long.class), any());
    }

    private static ProductResponse response(
            Long id,
            Long categoryId,
            String categoryName,
            String sku,
            String barcode,
            boolean active
    ) {
        return new ProductResponse(
                id,
                categoryId,
                categoryName,
                sku,
                barcode,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                active,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
