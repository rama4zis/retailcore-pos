package com.retailcore.pos.refund;

import com.retailcore.pos.sale.SaleEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Instant refundedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefundItemEntity> items = new ArrayList<>();

    private RefundEntity(SaleEntity sale, List<RefundItemEntity> items, String reason) {
        this.sale = sale;
        this.reason = normalizeOptional(reason);
        this.refundedAt = Instant.now();
        items.forEach(this::addItem);
        this.totalAmount = this.items.stream()
                .map(RefundItemEntity::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static RefundEntity create(SaleEntity sale, List<RefundItemEntity> items, String reason) {
        return new RefundEntity(sale, items, reason);
    }

    private void addItem(RefundItemEntity item) {
        item.attachTo(this);
        this.items.add(item);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
