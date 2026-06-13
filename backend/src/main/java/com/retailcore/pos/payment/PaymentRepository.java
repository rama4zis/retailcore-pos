package com.retailcore.pos.payment;

import com.retailcore.pos.report.dto.PaymentMethodSummaryResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsBySaleId(Long saleId);

    @Override
    @EntityGraph(attributePaths = {"sale"})
    List<PaymentEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"sale"})
    Optional<PaymentEntity> findById(Long id);

    @Query("""
            select new com.retailcore.pos.report.dto.PaymentMethodSummaryResponse(
                payment.method,
                count(payment),
                coalesce(sum(payment.amount), 0)
            )
            from PaymentEntity payment
            group by payment.method
            order by payment.method asc
            """)
    List<PaymentMethodSummaryResponse> summarizeByPaymentMethod();
}
