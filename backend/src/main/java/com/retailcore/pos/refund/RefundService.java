package com.retailcore.pos.refund;

import com.retailcore.pos.inventory.InventoryStockEntity;
import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementRepository;
import com.retailcore.pos.refund.dto.RefundItemRequest;
import com.retailcore.pos.refund.dto.RefundRequest;
import com.retailcore.pos.refund.dto.RefundResponse;
import com.retailcore.pos.sale.SaleEntity;
import com.retailcore.pos.sale.SaleItemEntity;
import com.retailcore.pos.sale.SaleNotFoundException;
import com.retailcore.pos.sale.SaleRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final SaleRepository saleRepository;
    private final RefundRepository refundRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public RefundResponse refund(Long saleId, RefundRequest request) {
        SaleEntity sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        List<RefundEntity> existingRefunds = refundRepository.findBySaleId(saleId);
        Map<Long, Integer> requestedQuantitiesByProduct = request.items()
                .stream()
                .collect(Collectors.toMap(RefundItemRequest::productId, RefundItemRequest::quantity, Integer::sum));
        Map<Long, Integer> soldQuantitiesByProduct = soldQuantitiesByProduct(sale);
        Map<Long, Integer> refundedQuantitiesByProduct = refundedQuantitiesByProduct(existingRefunds);
        Map<Long, SaleItemEntity> saleItemsByProduct = saleItemsByProduct(sale);

        List<RefundItemEntity> refundItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requestedQuantitiesByProduct.entrySet()) {
            Long productId = entry.getKey();
            int requestedQuantity = entry.getValue();
            SaleItemEntity saleItem = saleItemsByProduct.get(productId);
            if (saleItem == null) {
                throw new SaleItemNotRefundableException(saleId, productId);
            }

            int soldQuantity = soldQuantitiesByProduct.getOrDefault(productId, 0);
            int alreadyRefundedQuantity = refundedQuantitiesByProduct.getOrDefault(productId, 0);
            int refundableQuantity = soldQuantity - alreadyRefundedQuantity;
            if (requestedQuantity > refundableQuantity) {
                throw new RefundQuantityExceededException(productId, requestedQuantity, refundableQuantity);
            }

            refundItems.add(RefundItemEntity.create(saleItem, requestedQuantity));
        }

        RefundEntity refund = RefundEntity.create(sale, refundItems, request.reason());
        BigDecimal totalRefundedAmount = existingRefunds.stream()
                .map(RefundEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(refund.getTotalAmount());
        if (totalRefundedAmount.compareTo(sale.getTotalAmount()) > 0) {
            throw new RefundAmountExceededException();
        }

        restockReturnedItems(refund, request.reason());
        if (totalRefundedAmount.compareTo(sale.getTotalAmount()) == 0) {
            sale.markRefunded();
        } else {
            sale.markPartiallyRefunded();
        }

        return RefundResponse.from(refundRepository.save(refund));
    }

    private void restockReturnedItems(RefundEntity refund, String reason) {
        for (RefundItemEntity item : refund.getItems()) {
            InventoryStockEntity stock = inventoryStockRepository.findByProductId(item.getProduct().getId())
                    .orElseGet(() -> new InventoryStockEntity(item.getProduct()));
            stock.adjust(item.getQuantity());
            InventoryStockEntity savedStock = inventoryStockRepository.save(stock);
            stockMovementRepository.save(StockMovementEntity.refund(
                    item.getProduct(),
                    item.getQuantity(),
                    savedStock.getQuantity(),
                    movementReason(reason)
            ));
        }
    }

    private static Map<Long, Integer> soldQuantitiesByProduct(SaleEntity sale) {
        return sale.getItems()
                .stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), SaleItemEntity::getQuantity, Integer::sum));
    }

    private static Map<Long, Integer> refundedQuantitiesByProduct(List<RefundEntity> refunds) {
        Map<Long, Integer> refundedQuantities = new HashMap<>();
        refunds.stream()
                .flatMap(refund -> refund.getItems().stream())
                .forEach(item -> refundedQuantities.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum));
        return refundedQuantities;
    }

    private static Map<Long, SaleItemEntity> saleItemsByProduct(SaleEntity sale) {
        return sale.getItems()
                .stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), Function.identity(), (first, ignored) -> first));
    }

    private static String movementReason(String reason) {
        return reason == null || reason.isBlank() ? "Sale refund" : "Sale refund: " + reason.trim();
    }
}
