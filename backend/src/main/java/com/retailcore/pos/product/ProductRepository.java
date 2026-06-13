package com.retailcore.pos.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    List<ProductEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<ProductEntity> findById(Long id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsByBarcodeIgnoreCase(String barcode);

    boolean existsByBarcodeIgnoreCaseAndIdNot(String barcode, Long id);

    boolean existsByCategoryId(Long categoryId);
}
