package com.retailcore.pos.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.inventory.InventoryStockEntity;
import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementRepository;
import com.retailcore.pos.inventory.StockMovementType;
import com.retailcore.pos.product.ProductDetails;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.refund.dto.RefundItemRequest;
import com.retailcore.pos.refund.dto.RefundRequest;
import com.retailcore.pos.refund.dto.RefundResponse;
import com.retailcore.pos.sale.SaleEntity;
import com.retailcore.pos.sale.SaleItemEntity;
import com.retailcore.pos.sale.SaleNotFoundException;
import com.retailcore.pos.sale.SaleRepository;
import com.retailcore.pos.sale.SaleStatus;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRole;
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
class RefundServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private RefundService refundService;

    @Test
    void refundReturnsItemsToStockRecordsMovementAndMarksSalePartiallyRefunded() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00");
        SaleEntity sale = sale(product, 2);
        InventoryStockEntity stock = stock(product, 3);
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(refundRepository.findBySaleId(50L)).thenReturn(List.of());
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(inventoryStockRepository.save(stock)).thenReturn(stock);
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refundRepository.save(any(RefundEntity.class))).thenAnswer(invocation -> {
            RefundEntity refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 80L);
            return refund;
        });

        RefundResponse response = refundService.refund(50L, request(10L, 1, "Customer return"));

        ArgumentCaptor<RefundEntity> refundCaptor = ArgumentCaptor.forClass(RefundEntity.class);
        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(refundRepository).save(refundCaptor.capture());
        verify(stockMovementRepository).save(movementCaptor.capture());

        RefundEntity refund = refundCaptor.getValue();
        StockMovementEntity movement = movementCaptor.getValue();
        assertThat(stock.getQuantity()).isEqualTo(4);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PARTIALLY_REFUNDED);
        assertThat(refund.getTotalAmount()).isEqualByComparingTo("3500.00");
        assertThat(refund.getItems()).hasSize(1);
        assertThat(refund.getItems().getFirst().getLineTotal()).isEqualByComparingTo("3500.00");
        assertThat(movement.getMovementType()).isEqualTo(StockMovementType.REFUND);
        assertThat(movement.getQuantityChange()).isEqualTo(1);
        assertThat(movement.getStockAfter()).isEqualTo(4);
        assertThat(movement.getReason()).isEqualTo("Sale refund: Customer return");
        assertThat(response.id()).isEqualTo(80L);
        assertThat(response.saleStatus()).isEqualTo(SaleStatus.PARTIALLY_REFUNDED);
        assertThat(response.totalAmount()).isEqualByComparingTo("3500.00");
    }

    @Test
    void refundMarksSaleRefundedWhenCumulativeRefundEqualsSaleTotal() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00");
        SaleEntity sale = sale(product, 2);
        RefundEntity existingRefund = RefundEntity.create(sale, List.of(RefundItemEntity.create(sale.getItems().getFirst(), 1)), "First return");
        InventoryStockEntity stock = stock(product, 4);
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(refundRepository.findBySaleId(50L)).thenReturn(List.of(existingRefund));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(inventoryStockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(any(RefundEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundResponse response = refundService.refund(50L, request(10L, 1, null));

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.REFUNDED);
        assertThat(response.saleStatus()).isEqualTo(SaleStatus.REFUNDED);
        assertThat(stock.getQuantity()).isEqualTo(5);
    }

    @Test
    void refundRejectsQuantityAboveRemainingSoldQuantity() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00");
        SaleEntity sale = sale(product, 2);
        RefundEntity existingRefund = RefundEntity.create(sale, List.of(RefundItemEntity.create(sale.getItems().getFirst(), 1)), "First return");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(refundRepository.findBySaleId(50L)).thenReturn(List.of(existingRefund));

        assertThatThrownBy(() -> refundService.refund(50L, request(10L, 2, null)))
                .isInstanceOf(RefundQuantityExceededException.class)
                .hasMessageContaining("requested 2, refundable 1");

        verify(inventoryStockRepository, never()).save(any());
        verify(refundRepository, never()).save(any(RefundEntity.class));
    }

    @Test
    void refundRejectsProductThatWasNotSoldInSale() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00");
        SaleEntity sale = sale(product, 2);
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(refundRepository.findBySaleId(50L)).thenReturn(List.of());

        assertThatThrownBy(() -> refundService.refund(50L, request(99L, 1, null)))
                .isInstanceOf(SaleItemNotRefundableException.class)
                .hasMessageContaining("Product id 99 is not part of sale id 50");

        verify(inventoryStockRepository, never()).save(any());
        verify(refundRepository, never()).save(any(RefundEntity.class));
    }

    @Test
    void refundRejectsMissingSale() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refundService.refund(99L, request(10L, 1, null)))
                .isInstanceOf(SaleNotFoundException.class)
                .hasMessageContaining("Sale not found with id: 99");
    }

    private static RefundRequest request(Long productId, int quantity, String reason) {
        return new RefundRequest(List.of(new RefundItemRequest(productId, quantity)), reason);
    }

    private static InventoryStockEntity stock(ProductEntity product, int quantity) {
        InventoryStockEntity stock = new InventoryStockEntity(product);
        stock.adjust(quantity);
        return stock;
    }

    private static SaleEntity sale(ProductEntity product, int quantity) {
        SaleItemEntity item = ReflectionTestUtils.invokeMethod(SaleItemEntity.class, "create", product, quantity);
        SaleEntity sale = SaleEntity.complete(cashier(), List.of(item));
        ReflectionTestUtils.setField(sale, "id", 50L);
        ReflectionTestUtils.setField(sale, "saleNumber", "SALE-001");
        return sale;
    }

    private static ProductEntity product(Long id, String sku, String name, String price) {
        CategoryEntity category = new CategoryEntity("Beverages", null);
        ReflectionTestUtils.setField(category, "id", 1L);
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                sku,
                null,
                name,
                null,
                new BigDecimal(price),
                true
        ));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private static UserEntity cashier() {
        UserEntity user = new UserEntity("cashier@example.com", "Cashier One", "hash", UserRole.CASHIER, true);
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }
}
