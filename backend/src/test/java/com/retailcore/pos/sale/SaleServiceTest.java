package com.retailcore.pos.sale;

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
import com.retailcore.pos.product.ProductRepository;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import com.retailcore.pos.sale.dto.CheckoutItemRequest;
import com.retailcore.pos.sale.dto.CheckoutRequest;
import com.retailcore.pos.sale.dto.SaleResponse;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRepository;
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
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void checkoutCopiesProductPriceReducesStockAndRecordsSaleMovement() {
        UserEntity cashier = cashier();
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00", true);
        InventoryStockEntity stock = stock(product, 5);
        when(userRepository.findByEmailIgnoreCase("cashier@example.com")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(inventoryStockRepository.save(stock)).thenReturn(stock);
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleRepository.save(any(SaleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleResponse response = saleService.checkout(
                "cashier@example.com",
                new CheckoutRequest(List.of(new CheckoutItemRequest(10L, 2)))
        );

        ArgumentCaptor<SaleEntity> saleCaptor = ArgumentCaptor.forClass(SaleEntity.class);
        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(saleRepository).save(saleCaptor.capture());
        verify(stockMovementRepository).save(movementCaptor.capture());

        SaleEntity sale = saleCaptor.getValue();
        StockMovementEntity movement = movementCaptor.getValue();

        assertThat(stock.getQuantity()).isEqualTo(3);
        assertThat(sale.getSaleNumber()).startsWith("SALE-");
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("7000.00");
        assertThat(sale.getItems()).hasSize(1);
        assertThat(sale.getItems().getFirst().getUnitPrice()).isEqualByComparingTo("3500.00");
        assertThat(sale.getItems().getFirst().getLineTotal()).isEqualByComparingTo("7000.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("7000.00");
        assertThat(response.items().getFirst().sku()).isEqualTo("SKU-001");
        assertThat(movement.getMovementType()).isEqualTo(StockMovementType.SALE);
        assertThat(movement.getQuantityChange()).isEqualTo(-2);
        assertThat(movement.getStockAfter()).isEqualTo(3);
    }

    @Test
    void checkoutRejectsMissingProduct() {
        when(userRepository.findByEmailIgnoreCase("cashier@example.com")).thenReturn(Optional.of(cashier()));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.checkout(
                "cashier@example.com",
                new CheckoutRequest(List.of(new CheckoutItemRequest(99L, 1)))
        ))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(inventoryStockRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsInactiveProduct() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00", false);
        when(userRepository.findByEmailIgnoreCase("cashier@example.com")).thenReturn(Optional.of(cashier()));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> saleService.checkout(
                "cashier@example.com",
                new CheckoutRequest(List.of(new CheckoutItemRequest(10L, 1)))
        ))
                .isInstanceOf(InactiveProductSaleException.class)
                .hasMessageContaining("Cannot sell inactive product with id: 10");

        verify(inventoryStockRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsInsufficientStock() {
        ProductEntity product = product(10L, "SKU-001", "Mineral Water", "3500.00", true);
        InventoryStockEntity stock = stock(product, 1);
        when(userRepository.findByEmailIgnoreCase("cashier@example.com")).thenReturn(Optional.of(cashier()));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> saleService.checkout(
                "cashier@example.com",
                new CheckoutRequest(List.of(new CheckoutItemRequest(10L, 2)))
        ))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 2, available 1");

        verify(inventoryStockRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void findByIdRejectsMissingSale() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.findById(99L))
                .isInstanceOf(SaleNotFoundException.class)
                .hasMessageContaining("Sale not found with id: 99");
    }

    private static InventoryStockEntity stock(ProductEntity product, int quantity) {
        InventoryStockEntity stock = new InventoryStockEntity(product);
        stock.adjust(quantity);
        return stock;
    }

    private static ProductEntity product(Long id, String sku, String name, String price, boolean active) {
        CategoryEntity category = new CategoryEntity("Beverages", null);
        ReflectionTestUtils.setField(category, "id", 1L);
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                sku,
                null,
                name,
                null,
                new BigDecimal(price),
                active
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
