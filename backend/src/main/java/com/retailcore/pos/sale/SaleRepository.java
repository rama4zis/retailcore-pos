package com.retailcore.pos.sale;

import com.retailcore.pos.report.dto.CashierSalesReportResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"cashier", "items", "items.product"})
    List<SaleEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"cashier", "items", "items.product"})
    Optional<SaleEntity> findById(Long id);

    @Query("""
            select sum(sale.totalAmount)
            from SaleEntity sale
            where sale.completedAt >= :startInclusive
              and sale.completedAt < :endExclusive
            """)
    BigDecimal sumTotalAmountByCompletedAtBetween(
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

    @Query("""
            select new com.retailcore.pos.report.dto.CashierSalesReportResponse(
                cashier.id,
                cashier.name,
                cashier.email,
                count(sale),
                coalesce(sum(sale.totalAmount), 0)
            )
            from SaleEntity sale
            join sale.cashier cashier
            group by cashier.id, cashier.name, cashier.email
            order by sum(sale.totalAmount) desc, cashier.name asc
            """)
    List<CashierSalesReportResponse> findSalesByCashier();
}
