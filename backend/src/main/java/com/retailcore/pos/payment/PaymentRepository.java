package com.retailcore.pos.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsBySaleId(Long saleId);

    @Override
    @EntityGraph(attributePaths = {"sale"})
    List<PaymentEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"sale"})
    Optional<PaymentEntity> findById(Long id);
}
