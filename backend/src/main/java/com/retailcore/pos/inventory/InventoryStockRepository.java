package com.retailcore.pos.inventory;

import com.retailcore.pos.inventory.dto.InventoryStockResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "product")
    List<InventoryStockEntity> findAll();

    @EntityGraph(attributePaths = "product")
    Optional<InventoryStockEntity> findByProductId(Long productId);

    @EntityGraph(attributePaths = "product")
    List<InventoryStockEntity> findByQuantityLessThanEqual(int quantity);

    @Query("""
            select new com.retailcore.pos.inventory.dto.InventoryStockResponse(
                product.id,
                product.sku,
                product.name,
                stock.quantity,
                stock.lowStockThreshold,
                true,
                stock.createdAt,
                stock.updatedAt
            )
            from InventoryStockEntity stock
            join stock.product product
            where stock.quantity <= stock.lowStockThreshold
            order by stock.quantity asc, product.name asc
            """)
    List<InventoryStockResponse> findLowStock();
}
