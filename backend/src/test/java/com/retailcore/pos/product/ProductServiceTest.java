package com.retailcore.pos.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.category.CategoryRepository;
import com.retailcore.pos.category.exception.CategoryNotFoundException;
import com.retailcore.pos.common.exception.DuplicateResourceException;
import com.retailcore.pos.product.dto.ProductActiveRequest;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;
import com.retailcore.pos.product.dto.ProductUpdateRequest;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createDefaultsActiveToTrueAndTrimsIdentifiers() {
        CategoryEntity category = category(1L, "Beverages");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCase("BAR-001")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = createRequest(
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
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.categoryName()).isEqualTo("Beverages");
    }

    @Test
    void createRejectsDuplicateSku() {
        CategoryEntity category = category(1L, "Beverages");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(true);

        ProductCreateRequest request = createRequest(
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
        CategoryEntity category = category(1L, "Beverages");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCase("BAR-001")).thenReturn(true);

        ProductCreateRequest request = createRequest(
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
        CategoryEntity category = category(1L, "Beverages");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = createRequest(
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

        ProductCreateRequest request = createRequest(
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
    void findAllReturnsProductResponses() {
        ProductEntity product = product(10L, category(1L, "Beverages"), "SKU-001", "BAR-001", true);
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(10L);
        assertThat(responses.getFirst().categoryName()).isEqualTo("Beverages");
        assertThat(responses.getFirst().sku()).isEqualTo("SKU-001");
    }

    @Test
    void findByIdReturnsProductResponse() {
        ProductEntity product = product(10L, category(1L, "Beverages"), "SKU-001", null, true);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("SKU-001");
    }

    @Test
    void findByIdRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void updateChangesProductDetails() {
        CategoryEntity oldCategory = category(1L, "Beverages");
        CategoryEntity newCategory = category(2L, "Snacks");
        ProductEntity product = product(10L, oldCategory, "SKU-001", "BAR-001", true);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("SKU-002", 10L)).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCaseAndIdNot("BAR-002", 10L)).thenReturn(false);

        ProductUpdateRequest request = updateRequest(
                2L,
                " SKU-002 ",
                " BAR-002 ",
                "Potato Chips",
                "Crispy snack",
                new BigDecimal("12000.00"),
                false
        );

        ProductResponse response = productService.update(10L, request);

        assertThat(product.getCategory()).isSameAs(newCategory);
        assertThat(product.getSku()).isEqualTo("SKU-002");
        assertThat(product.getBarcode()).isEqualTo("BAR-002");
        assertThat(product.getName()).isEqualTo("Potato Chips");
        assertThat(product.getDescription()).isEqualTo("Crispy snack");
        assertThat(product.getPrice()).isEqualByComparingTo("12000.00");
        assertThat(product.isActive()).isFalse();
        assertThat(response.categoryId()).isEqualTo(2L);
    }

    @Test
    void updateRejectsDuplicateSku() {
        CategoryEntity category = category(1L, "Beverages");
        ProductEntity product = product(10L, category, "SKU-001", null, true);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("SKU-002", 10L)).thenReturn(true);

        ProductUpdateRequest request = updateRequest(
                1L,
                "SKU-002",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.update(10L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product SKU already exists: SKU-002");
    }

    @Test
    void updateRejectsDuplicateBarcode() {
        CategoryEntity category = category(1L, "Beverages");
        ProductEntity product = product(10L, category, "SKU-001", "BAR-001", true);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("SKU-001", 10L)).thenReturn(false);
        when(productRepository.existsByBarcodeIgnoreCaseAndIdNot("BAR-002", 10L)).thenReturn(true);

        ProductUpdateRequest request = updateRequest(
                1L,
                "SKU-001",
                "BAR-002",
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.update(10L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product barcode already exists: BAR-002");
    }

    @Test
    void updateRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductUpdateRequest request = updateRequest(
                1L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void updateRejectsMissingCategory() {
        ProductEntity product = product(10L, category(1L, "Beverages"), "SKU-001", null, true);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ProductUpdateRequest request = updateRequest(
                99L,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                true
        );

        assertThatThrownBy(() -> productService.update(10L, request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with id: 99");
    }

    @Test
    void changeActiveUpdatesProductStatus() {
        ProductEntity product = product(1L, category(1L, "Beverages"), "SKU-001", null, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.changeActive(1L, new ProductActiveRequest(false));

        assertThat(product.isActive()).isFalse();
        assertThat(response.active()).isFalse();
    }

    @Test
    void changeActiveRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.changeActive(99L, new ProductActiveRequest(false)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    private static CategoryEntity category(Long id, String name) {
        CategoryEntity category = new CategoryEntity(name, null);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private static ProductEntity product(
            Long id,
            CategoryEntity category,
            String sku,
            String barcode,
            boolean active
    ) {
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                sku,
                barcode,
                "Mineral Water",
                null,
                new BigDecimal("3500.00"),
                active
        ));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private static ProductCreateRequest createRequest(
            Long categoryId,
            String sku,
            String barcode,
            String name,
            String description,
            BigDecimal price,
            Boolean active
    ) {
        return new ProductCreateRequest(categoryId, sku, barcode, name, description, price, active);
    }

    private static ProductUpdateRequest updateRequest(
            Long categoryId,
            String sku,
            String barcode,
            String name,
            String description,
            BigDecimal price,
            Boolean active
    ) {
        return new ProductUpdateRequest(categoryId, sku, barcode, name, description, price, active);
    }
}
