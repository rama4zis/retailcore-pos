package com.retailcore.pos.product;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.product.dto.ProductActiveRequest;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;
import com.retailcore.pos.product.dto.ProductUpdateRequest;
import com.retailcore.pos.category.exception.CategoryNotFoundException;
import com.retailcore.pos.product.exception.DuplicateProductBarcodeException;
import com.retailcore.pos.product.exception.DuplicateProductSkuException;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import com.retailcore.pos.category.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        ProductDetails details = productDetailsFrom(request);
        ensureSkuAvailable(details.sku());
        ensureBarcodeAvailable(details.barcode());

        ProductEntity product = ProductEntity.create(details);
        ProductEntity savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        ProductEntity product = getProduct(id);
        ProductDetails details = productDetailsFrom(request);

        ensureSkuAvailableForUpdate(details.sku(), id);
        ensureBarcodeAvailableForUpdate(details.barcode(), id);

        product.update(details);

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse changeActive(Long id, ProductActiveRequest request) {
        ProductEntity product = getProduct(id);
        product.changeActive(request.active());

        return ProductResponse.from(product);
    }

    private ProductDetails productDetailsFrom(ProductCreateRequest request) {
        return new ProductDetails(
                getCategory(request.categoryId()),
                request.sku(),
                request.barcode(),
                request.name(),
                request.description(),
                request.price(),
                request.activeOrDefault()
        );
    }

    private ProductDetails productDetailsFrom(ProductUpdateRequest request) {
        return new ProductDetails(
                getCategory(request.categoryId()),
                request.sku(),
                request.barcode(),
                request.name(),
                request.description(),
                request.price(),
                request.active()
        );
    }

    private ProductEntity getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private CategoryEntity getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private void ensureSkuAvailable(String sku) {
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateProductSkuException(sku);
        }
    }

    private void ensureSkuAvailableForUpdate(String sku, Long id) {
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new DuplicateProductSkuException(sku);
        }
    }

    private void ensureBarcodeAvailable(String barcode) {
        if (barcode != null && productRepository.existsByBarcodeIgnoreCase(barcode)) {
            throw new DuplicateProductBarcodeException(barcode);
        }
    }

    private void ensureBarcodeAvailableForUpdate(String barcode, Long id) {
        if (barcode != null && productRepository.existsByBarcodeIgnoreCaseAndIdNot(barcode, id)) {
            throw new DuplicateProductBarcodeException(barcode);
        }
    }
}
