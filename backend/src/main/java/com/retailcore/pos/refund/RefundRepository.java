package com.retailcore.pos.refund;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<RefundEntity, Long> {

    @EntityGraph(attributePaths = {"sale", "items", "items.saleItem", "items.product"})
    List<RefundEntity> findBySaleId(Long saleId);
}
