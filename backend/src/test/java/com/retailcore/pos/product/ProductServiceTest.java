package com.retailcore.pos.product;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.category.CategoryRepository;
import com.retailcore.pos.category.exception.CategoryNotFoundException;
import com.retailcore.pos.common.exception.DuplicateResourceException;
import com.retailcore.pos.product.dto.ProductActiveRequest;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createDefaultsActiveToTrueAndTrimsIdentifiers() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCase("BAR-001")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                " SKU-001 ",
                " BAR-001 ",
                "Mineral Water",
                "Bottle",
                new BigDecimal("3500.00"),
                null
        );

        ProductResponse response = productService.create(request);

        ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(productCaptor.capture());
        ProductEntity saved = productCaptor.getValue();

        assertThat(saved.getSku()).isEqualTo("SKU-001");
        assertThat(saved.getBarcode()).isEqualTo("BAR-001");
        assertThat(saved.isActive()).isTrue();
        assertThat(response.sku()).isEqualTo("SKU-001");
    }

    @Test
    void createRejectsDuplicateSku() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(true);

        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product SKU already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateBarcodeWhenProvided() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCase("BAR-001")).thenReturn(true);

        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "SKU-001",
                "BAR-001",
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product barcode already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createAllowsBlankBarcodeAsNull() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "SKU-001",
                " ",
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        productService.create(request);

        ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getBarcode()).isNull();
    }

    @Test
    void createRejectsMissingCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ProductCreateRequest request = new ProductCreateRequest(
                99L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with id: 99");

        verify(productRepository, never()).save(any());
    }

    @Test
    void changeActiveUpdatesProductStatus() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        ));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.changeActive(1L, new ProductActiveRequest(false));

        assertThat(product.isActive()).isFalse();
        assertThat(response.active()).isFalse();
    }
}
