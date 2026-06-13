package com.retailcore.pos.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import com.retailcore.pos.inventory.dto.StockAdjustmentRequest;
import com.retailcore.pos.inventory.dto.StockMovementResponse;
import com.retailcore.pos.product.ProductDetails;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.product.ProductRepository;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void adjustCreatesStockWhenMissingAndRecordsMovement() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.empty());
        when(inventoryStockRepository.save(any(InventoryStockEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryStockResponse response = inventoryService.adjust(
                10L,
                new StockAdjustmentRequest(5, 2, "Opening stock")
        );

        ArgumentCaptor<InventoryStockEntity> stockCaptor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(inventoryStockRepository).save(stockCaptor.capture());
        verify(stockMovementRepository).save(movementCaptor.capture());

        InventoryStockEntity savedStock = stockCaptor.getValue();
        StockMovementEntity movement = movementCaptor.getValue();

        assertThat(savedStock.getProduct()).isSameAs(product);
        assertThat(savedStock.getQuantity()).isEqualTo(5);
        assertThat(savedStock.getLowStockThreshold()).isEqualTo(2);
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(response.lowStock()).isFalse();
        assertThat(movement.getProduct()).isSameAs(product);
        assertThat(movement.getMovementType()).isEqualTo(StockMovementType.ADJUSTMENT);
        assertThat(movement.getQuantityChange()).isEqualTo(5);
        assertThat(movement.getStockAfter()).isEqualTo(5);
        assertThat(movement.getReason()).isEqualTo("Opening stock");
    }

    @Test
    void adjustUpdatesExistingStockAndTrimsMovementReason() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        InventoryStockEntity stock = stock(product, 8, 3);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(inventoryStockRepository.save(stock)).thenReturn(stock);
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryStockResponse response = inventoryService.adjust(
                10L,
                new StockAdjustmentRequest(-3, null, " Damaged item ")
        );

        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        assertThat(stock.getQuantity()).isEqualTo(5);
        assertThat(stock.getLowStockThreshold()).isEqualTo(3);
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(movementCaptor.getValue().getReason()).isEqualTo("Damaged item");
        assertThat(movementCaptor.getValue().getQuantityChange()).isEqualTo(-3);
    }

    @Test
    void adjustRejectsNegativeStockAndDoesNotRecordMovement() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        InventoryStockEntity stock = stock(product, 2, 0);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> inventoryService.adjust(10L, new StockAdjustmentRequest(-3, null, "Loss")))
                .isInstanceOf(NegativeStockException.class)
                .hasMessageContaining("Stock cannot become negative");

        verify(inventoryStockRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustRejectsZeroChange() {
        assertThatThrownBy(() -> inventoryService.adjust(10L, new StockAdjustmentRequest(0, null, "No-op")))
                .isInstanceOf(InvalidStockAdjustmentException.class)
                .hasMessageContaining("Quantity change must not be zero");

        verify(productRepository, never()).findById(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.adjust(99L, new StockAdjustmentRequest(5, null, null)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(inventoryStockRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void findByProductIdReturnsStock() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        InventoryStockEntity stock = stock(product, 4, 5);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));

        InventoryStockResponse response = inventoryService.findByProductId(10L);

        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.sku()).isEqualTo("SKU-001");
        assertThat(response.quantity()).isEqualTo(4);
        assertThat(response.lowStock()).isTrue();
    }

    @Test
    void findByProductIdRejectsMissingStock() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.findByProductId(10L))
                .isInstanceOf(InventoryStockNotFoundException.class)
                .hasMessageContaining("Inventory stock not found for product id: 10");
    }

    @Test
    void findAllReturnsInventoryStocks() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        when(inventoryStockRepository.findAll()).thenReturn(List.of(stock(product, 6, 2)));

        List<InventoryStockResponse> responses = inventoryService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().productId()).isEqualTo(10L);
        assertThat(responses.getFirst().quantity()).isEqualTo(6);
    }

    @Test
    void findLowStockFiltersStockAtOrBelowThreshold() {
        ProductEntity firstProduct = product(10L, "SKU-001", "Mineral Water");
        ProductEntity secondProduct = product(11L, "SKU-002", "Potato Chips");
        when(inventoryStockRepository.findAll()).thenReturn(List.of(
                stock(firstProduct, 2, 3),
                stock(secondProduct, 8, 3)
        ));

        List<InventoryStockResponse> responses = inventoryService.findLowStock();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().productId()).isEqualTo(10L);
    }

    @Test
    void findMovementsReturnsProductMovements() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water");
        StockMovementEntity movement = StockMovementEntity.adjustment(product, 5, 5, "Opening stock");
        ReflectionTestUtils.setField(movement, "id", 20L);
        when(productRepository.existsById(10L)).thenReturn(true);
        when(stockMovementRepository.findByProductIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(movement));

        List<StockMovementResponse> responses = inventoryService.findMovements(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(20L);
        assertThat(responses.getFirst().quantityChange()).isEqualTo(5);
        assertThat(responses.getFirst().stockAfter()).isEqualTo(5);
    }

    @Test
    void findMovementsRejectsMissingProduct() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.findMovements(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(stockMovementRepository, never()).findByProductIdOrderByCreatedAtDesc(any());
    }

    private static InventoryStockEntity stock(ProductEntity product, int quantity, int lowStockThreshold) {
        InventoryStockEntity stock = new InventoryStockEntity(product);
        stock.adjust(quantity);
        stock.changeLowStockThreshold(lowStockThreshold);
        return stock;
    }

    private static ProductEntity product(Long id, String sku, String name) {
        CategoryEntity category = new CategoryEntity("Beverages", null);
        ReflectionTestUtils.setField(category, "id", 1L);
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                sku,
                null,
                name,
                null,
                new BigDecimal("3500.00"),
                true
        ));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
