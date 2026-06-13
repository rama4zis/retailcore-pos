package com.retailcore.pos.inventory;

import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.inventory.dto.StockAdjustmentRequest;
import com.retailcore.pos.inventory.dto.StockMovementResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryStockResponse>> findAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryStockResponse>> findLowStock() {
        return ResponseEntity.ok(inventoryService.findLowStock());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryStockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryStockResponse> adjust(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return ResponseEntity.ok(inventoryService.adjust(productId, request));
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findMovements(productId));
    }
}
