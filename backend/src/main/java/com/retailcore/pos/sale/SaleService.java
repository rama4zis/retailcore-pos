package com.retailcore.pos.sale;

import com.retailcore.pos.inventory.InventoryStockEntity;
import com.retailcore.pos.inventory.InventoryStockRepository;
import com.retailcore.pos.inventory.StockMovementEntity;
import com.retailcore.pos.inventory.StockMovementRepository;
import com.retailcore.pos.payment.CardCashTenderedException;
import com.retailcore.pos.payment.InsufficientCashTenderedException;
import com.retailcore.pos.payment.PaymentAmountMismatchException;
import com.retailcore.pos.payment.PaymentEntity;
import com.retailcore.pos.payment.PaymentRepository;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.product.ProductRepository;
import com.retailcore.pos.product.exception.ProductNotFoundException;
import com.retailcore.pos.receipt.dto.ReceiptResponse;
import com.retailcore.pos.sale.dto.CheckoutItemRequest;
import com.retailcore.pos.sale.dto.CheckoutPaymentRequest;
import com.retailcore.pos.sale.dto.CheckoutRequest;
import com.retailcore.pos.sale.dto.SaleResponse;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserNotFoundException;
import com.retailcore.pos.user.UserRepository;
import java.math.BigDecimal;
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
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReceiptResponse checkout(String cashierEmail, CheckoutRequest request) {
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
        ensurePaymentAmountMatchesSaleTotal(request.payment().amount(), sale.getTotalAmount());

        SaleEntity savedSale = saleRepository.save(sale);
        PaymentEntity payment = createPayment(savedSale, request.payment());
        PaymentEntity savedPayment = paymentRepository.save(payment);

        return ReceiptResponse.from(savedSale, savedPayment);
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

    private static PaymentEntity createPayment(SaleEntity sale, CheckoutPaymentRequest paymentRequest) {
        return switch (paymentRequest.method()) {
            case CASH -> createCashPayment(sale, paymentRequest.amount(), paymentRequest.cashTendered());
            case CARD -> createCardPayment(sale, paymentRequest.amount(), paymentRequest.cashTendered());
        };
    }

    private static PaymentEntity createCashPayment(SaleEntity sale, BigDecimal amount, BigDecimal cashTendered) {
        if (cashTendered == null) {
            throw new InsufficientCashTenderedException();
        }
        if (cashTendered.compareTo(amount) < 0) {
            throw new InsufficientCashTenderedException(cashTendered, amount);
        }
        return PaymentEntity.cash(sale, amount, cashTendered);
    }

    private static PaymentEntity createCardPayment(SaleEntity sale, BigDecimal amount, BigDecimal cashTendered) {
        if (cashTendered != null) {
            throw new CardCashTenderedException();
        }
        return PaymentEntity.card(sale, amount);
    }

    private static void ensurePaymentAmountMatchesSaleTotal(BigDecimal amount, BigDecimal saleTotal) {
        if (amount.compareTo(saleTotal) != 0) {
            throw new PaymentAmountMismatchException(amount, saleTotal);
        }
    }
}
