package com.retailcore.pos.payment;

import com.retailcore.pos.sale.SaleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    private SaleEntity sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashTendered;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal changeAmount;

    @Column(nullable = false)
    private Instant paidAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private PaymentEntity(
            SaleEntity sale,
            PaymentMethod method,
            BigDecimal amount,
            BigDecimal cashTendered,
            BigDecimal changeAmount
    ) {
        this.sale = sale;
        this.method = method;
        this.amount = amount;
        this.cashTendered = cashTendered;
        this.changeAmount = changeAmount;
        this.paidAt = Instant.now();
    }

    public static PaymentEntity cash(SaleEntity sale, BigDecimal amount, BigDecimal cashTendered) {
        return new PaymentEntity(sale, PaymentMethod.CASH, amount, cashTendered, cashTendered.subtract(amount));
    }

    public static PaymentEntity card(SaleEntity sale, BigDecimal amount) {
        return new PaymentEntity(sale, PaymentMethod.CARD, amount, null, BigDecimal.ZERO);
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
