package com.retailcore.pos.sale;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItemEntity, Long> {
}
