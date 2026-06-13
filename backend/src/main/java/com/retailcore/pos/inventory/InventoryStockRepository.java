package com.retailcore.pos.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "product")
    List<InventoryStockEntity> findAll();

    @EntityGraph(attributePaths = "product")
    Optional<InventoryStockEntity> findByProductId(Long productId);

    @EntityGraph(attributePaths = "product")
    List<InventoryStockEntity> findByQuantityLessThanEqual(int quantity);
}
