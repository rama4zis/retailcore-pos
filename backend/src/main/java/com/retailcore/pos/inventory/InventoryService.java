package com.retailcore.pos.inventory;

import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.inventory.dto.StockAdjustmentRequest;
import com.retailcore.pos.inventory.dto.StockMovementResponse;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.product.ProductRepository;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryStockRepository inventoryStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> findAll() {
        return inventoryStockRepository.findAll()
                .stream()
                .map(InventoryStockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryStockResponse findByProductId(Long productId) {
        return InventoryStockResponse.from(getStock(productId));
    }

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> findLowStock() {
        return inventoryStockRepository.findAll()
                .stream()
                .filter(stock -> stock.getQuantity() <= stock.getLowStockThreshold())
                .map(InventoryStockResponse::from)
                .toList();
    }

    @Transactional
    public InventoryStockResponse adjust(Long productId, StockAdjustmentRequest request) {
        if (request.quantityChange() == 0) {
            throw new InvalidStockAdjustmentException("Quantity change must not be zero");
        }

        ProductEntity product = getProduct(productId);
        InventoryStockEntity stock = inventoryStockRepository.findByProductId(productId)
                .orElseGet(() -> new InventoryStockEntity(product));

        if (request.lowStockThreshold() != null) {
            stock.changeLowStockThreshold(request.lowStockThreshold());
        }

        stock.adjust(request.quantityChange());
        InventoryStockEntity savedStock = inventoryStockRepository.save(stock);
        stockMovementRepository.save(StockMovementEntity.adjustment(
                product,
                request.quantityChange(),
                savedStock.getQuantity(),
                request.reason()
        ));

        return InventoryStockResponse.from(savedStock);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findMovements(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(StockMovementResponse::from)
                .toList();
    }

    private InventoryStockEntity getStock(Long productId) {
        getProduct(productId);
        return inventoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryStockNotFoundException(productId));
    }

    private ProductEntity getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
