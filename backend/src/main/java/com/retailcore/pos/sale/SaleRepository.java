package com.retailcore.pos.sale;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"cashier", "items", "items.product"})
    List<SaleEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"cashier", "items", "items.product"})
    Optional<SaleEntity> findById(Long id);
}
