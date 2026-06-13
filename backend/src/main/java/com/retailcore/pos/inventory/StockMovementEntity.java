package com.retailcore.pos.inventory;

import com.retailcore.pos.product.ProductEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_movements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StockMovementType movementType;

    @Column(nullable = false)
    private int quantityChange;

    @Column(nullable = false)
    private int stockAfter;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private StockMovementEntity(
            ProductEntity product,
            StockMovementType movementType,
            int quantityChange,
            int stockAfter,
            String reason
    ) {
        this.product = product;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.stockAfter = stockAfter;
        this.reason = normalizeOptional(reason);
    }

    public static StockMovementEntity adjustment(ProductEntity product, int quantityChange, int stockAfter, String reason) {
        return new StockMovementEntity(product, StockMovementType.ADJUSTMENT, quantityChange, stockAfter, reason);
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
