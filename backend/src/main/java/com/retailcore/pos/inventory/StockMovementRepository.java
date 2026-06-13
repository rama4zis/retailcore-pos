package com.retailcore.pos.inventory;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {

    @EntityGraph(attributePaths = "product")
    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(Long productId);
}
