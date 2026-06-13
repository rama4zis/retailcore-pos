package com.retailcore.pos.refund;

import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.sale.SaleItemEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refund_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refund_id", nullable = false)
    private RefundEntity refund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItemEntity saleItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 160)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    private RefundItemEntity(SaleItemEntity saleItem, int quantity) {
        this.saleItem = saleItem;
        this.product = saleItem.getProduct();
        this.sku = saleItem.getSku();
        this.productName = saleItem.getProductName();
        this.quantity = quantity;
        this.unitPrice = saleItem.getUnitPrice();
        this.lineTotal = saleItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    static RefundItemEntity create(SaleItemEntity saleItem, int quantity) {
        return new RefundItemEntity(saleItem, quantity);
    }

    void attachTo(RefundEntity refund) {
        this.refund = refund;
    }
}
