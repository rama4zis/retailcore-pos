package com.retailcore.pos.inventory;

import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.inventory.dto.StockAdjustmentRequest;
import com.retailcore.pos.inventory.dto.StockMovementResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inventory", description = "Stock level and stock movement endpoints")
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "List inventory", description = "Lists stock records for all products.")
    @GetMapping
    public ResponseEntity<List<InventoryStockResponse>> findAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @Operation(summary = "List low-stock products", description = "Lists inventory records at or below their low-stock threshold.")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryStockResponse>> findLowStock() {
        return ResponseEntity.ok(inventoryService.findLowStock());
    }

    @Operation(summary = "Get product inventory", description = "Returns stock details for one product.")
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryStockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @Operation(summary = "Adjust stock", description = "Applies a stock adjustment and records a stock movement.")
    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryStockResponse> adjust(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return ResponseEntity.ok(inventoryService.adjust(productId, request));
    }

    @Operation(summary = "List product stock movements", description = "Lists the stock movement history for one product.")
    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findMovements(productId));
    }
}

