package com.retailcore.pos.sale;

import com.retailcore.pos.user.UserEntity;
import jakarta.persistence.CascadeType;
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
@Table(name = "sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String saleNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cashier_id", nullable = false)
    private UserEntity cashier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SaleStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant completedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemEntity> items = new ArrayList<>();

    private SaleEntity(UserEntity cashier, List<SaleItemEntity> items) {
        this.saleNumber = SaleNumberGenerator.next();
        this.cashier = cashier;
        this.status = SaleStatus.COMPLETED;
        this.completedAt = Instant.now();
        items.forEach(this::addItem);
        this.totalAmount = this.items.stream()
                .map(SaleItemEntity::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static SaleEntity complete(UserEntity cashier, List<SaleItemEntity> items) {
        return new SaleEntity(cashier, items);
    }

    private void addItem(SaleItemEntity item) {
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
}
