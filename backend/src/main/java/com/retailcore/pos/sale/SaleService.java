package com.retailcore.pos.sale;

import com.retailcore.pos.inventory.InventoryStockEntity;
import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementRepository;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.product.ProductRepository;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import com.retailcore.pos.sale.dto.CheckoutItemRequest;
import com.retailcore.pos.sale.dto.CheckoutRequest;
import com.retailcore.pos.sale.dto.SaleResponse;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserNotFoundException;
import com.retailcore.pos.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    @Transactional
    public SaleResponse checkout(String cashierEmail, CheckoutRequest request) {
        UserEntity cashier = userRepository.findByEmailIgnoreCase(cashierEmail)
                .orElseThrow(() -> new UserNotFoundException(cashierEmail));
        List<SaleItemEntity> saleItems = new ArrayList<>();

        for (CheckoutItemRequest itemRequest : request.items()) {
            ProductEntity product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.productId()));
            if (!product.isActive()) {
                throw new InactiveProductSaleException(product.getId());
            }

            InventoryStockEntity stock = inventoryStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new InsufficientStockException(product.getId(), itemRequest.quantity(), 0));
            if (stock.getQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException(product.getId(), itemRequest.quantity(), stock.getQuantity());
            }

            stock.adjust(-itemRequest.quantity());
            InventoryStockEntity savedStock = inventoryStockRepository.save(stock);
            stockMovementRepository.save(StockMovementEntity.sale(
                    product,
                    -itemRequest.quantity(),
                    savedStock.getQuantity(),
                    "Sale checkout"
            ));
            saleItems.add(SaleItemEntity.create(product, itemRequest.quantity()));
        }

        SaleEntity sale = SaleEntity.complete(cashier, saleItems);
        return SaleResponse.from(saleRepository.save(sale));
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAll()
                .stream()
                .map(SaleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse findById(Long id) {
        return saleRepository.findById(id)
                .map(SaleResponse::from)
                .orElseThrow(() -> new SaleNotFoundException(id));
    }
}
