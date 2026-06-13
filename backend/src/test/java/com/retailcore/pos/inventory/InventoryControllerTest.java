package com.retailcore.pos.inventory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.inventory.dto.StockAdjustmentRequest;
import com.retailcore.pos.inventory.dto.StockMovementResponse;
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
        controllers = InventoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void findAllReturnsInventoryStocks() throws Exception {
        when(inventoryService.findAll()).thenReturn(List.of(stockResponse(10L, "SKU-001", 5, 2)));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    void findLowStockReturnsInventoryStocks() throws Exception {
        when(inventoryService.findLowStock()).thenReturn(List.of(stockResponse(10L, "SKU-001", 1, 2)));

        mockMvc.perform(get("/api/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void findByProductIdReturnsInventoryStock() throws Exception {
        when(inventoryService.findByProductId(10L)).thenReturn(stockResponse(10L, "SKU-001", 5, 2));

        mockMvc.perform(get("/api/inventory/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10L))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.lowStockThreshold").value(2));
    }

    @Test
    void adjustRequiresQuantityChange() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(null, 2, "Opening stock");

        mockMvc.perform(post("/api/inventory/10/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("quantityChange"));
    }

    @Test
    void adjustRejectsNegativeThreshold() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(5, -1, "Opening stock");

        mockMvc.perform(post("/api/inventory/10/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("lowStockThreshold"));
    }

    @Test
    void adjustReturnsUpdatedInventoryStock() throws Exception {
        when(inventoryService.adjust(any(Long.class), any(StockAdjustmentRequest.class)))
                .thenReturn(stockResponse(10L, "SKU-001", 5, 2));
        StockAdjustmentRequest request = new StockAdjustmentRequest(5, 2, "Opening stock");

        mockMvc.perform(post("/api/inventory/10/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10L))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.lowStock").value(false));

        verify(inventoryService).adjust(any(Long.class), any(StockAdjustmentRequest.class));
    }

    @Test
    void adjustReturnsConflictWhenStockWouldBecomeNegative() throws Exception {
        when(inventoryService.adjust(any(Long.class), any(StockAdjustmentRequest.class)))
                .thenThrow(new NegativeStockException(10L, -1));
        StockAdjustmentRequest request = new StockAdjustmentRequest(-3, null, "Loss");

        mockMvc.perform(post("/api/inventory/10/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock cannot become negative for product id 10: attempted quantity -1"));
    }

    @Test
    void findMovementsReturnsStockMovements() throws Exception {
        when(inventoryService.findMovements(10L)).thenReturn(List.of(movementResponse(20L, 10L)));

        mockMvc.perform(get("/api/inventory/10/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20L))
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$[0].quantityChange").value(5));
    }

    private static InventoryStockResponse stockResponse(Long productId, String sku, int quantity, int threshold) {
        return new InventoryStockResponse(
                productId,
                sku,
                "Mineral Water",
                quantity,
                threshold,
                quantity <= threshold,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    private static StockMovementResponse movementResponse(Long id, Long productId) {
        return new StockMovementResponse(
                id,
                productId,
                "SKU-001",
                StockMovementType.ADJUSTMENT,
                5,
                5,
                "Opening stock",
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
