package com.retailcore.pos.payment;

import com.retailcore.pos.payment.dto.PaymentCreateRequest;
import com.retailcore.pos.payment.dto.PaymentResponse;
import com.retailcore.pos.sale.SaleEntity;
import com.retailcore.pos.sale.SaleNotFoundException;
import com.retailcore.pos.sale.SaleRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public PaymentResponse create(PaymentCreateRequest request) {
        SaleEntity sale = saleRepository.findById(request.saleId())
                .orElseThrow(() -> new SaleNotFoundException(request.saleId()));

        ensureAmountMatchesSaleTotal(request.amount(), sale.getTotalAmount());
        ensureSaleHasNoPayment(request.saleId());

        PaymentEntity payment = switch (request.method()) {
            case CASH -> createCashPayment(sale, request.amount(), request.cashTendered());
            case CARD -> createCardPayment(sale, request.amount(), request.cashTendered());
        };

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void ensureSaleHasNoPayment(Long saleId) {
        if (paymentRepository.existsBySaleId(saleId)) {
            throw new DuplicatePaymentException(saleId);
        }
    }

    private static void ensureAmountMatchesSaleTotal(BigDecimal amount, BigDecimal saleTotal) {
        if (amount.compareTo(saleTotal) != 0) {
            throw new PaymentAmountMismatchException(amount, saleTotal);
        }
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
}
