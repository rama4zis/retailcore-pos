package com.retailcore.pos.sale;

import com.retailcore.pos.report.dto.TopSellingProductResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleItemRepository extends JpaRepository<SaleItemEntity, Long> {

    @Query("""
            select new com.retailcore.pos.report.dto.TopSellingProductResponse(
                product.id,
                item.sku,
                item.productName,
                sum(item.quantity),
                sum(item.lineTotal)
            )
            from SaleItemEntity item
            join item.product product
            group by product.id, item.sku, item.productName
            order by sum(item.quantity) desc, sum(item.lineTotal) desc, item.productName asc
            """)
    List<TopSellingProductResponse> findTopSellingProducts();
}
