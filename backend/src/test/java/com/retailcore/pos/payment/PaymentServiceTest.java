package com.retailcore.pos.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.category.CategoryEntity;
import com.retailcore.pos.payment.dto.PaymentCreateRequest;
import com.retailcore.pos.payment.dto.PaymentResponse;
import com.retailcore.pos.product.ProductDetails;
import com.retailcore.pos.product.ProductEntity;
import com.retailcore.pos.sale.SaleEntity;
import com.retailcore.pos.sale.SaleItemEntity;
import com.retailcore.pos.sale.SaleNotFoundException;
import com.retailcore.pos.sale.SaleRepository;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRole;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createCashPaymentCalculatesChangeWhenAmountMatchesSaleTotal() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(paymentRepository.existsBySaleId(50L)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CASH,
                new BigDecimal("7000.00"),
                new BigDecimal("10000.00")
        ));

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        PaymentEntity payment = paymentCaptor.getValue();

        assertThat(payment.getSale()).isSameAs(sale);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(payment.getAmount()).isEqualByComparingTo("7000.00");
        assertThat(payment.getCashTendered()).isEqualByComparingTo("10000.00");
        assertThat(payment.getChangeAmount()).isEqualByComparingTo("3000.00");
        assertThat(response.saleId()).isEqualTo(50L);
        assertThat(response.saleNumber()).isEqualTo("SALE-001");
        assertThat(response.changeAmount()).isEqualByComparingTo("3000.00");
    }

    @Test
    void createCardPaymentStoresZeroChange() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(paymentRepository.existsBySaleId(50L)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CARD,
                new BigDecimal("7000.00"),
                null
        ));

        assertThat(response.method()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.cashTendered()).isNull();
        assertThat(response.changeAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void createRejectsAmountThatDoesNotMatchSaleTotal() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CASH,
                new BigDecimal("6000.00"),
                new BigDecimal("6000.00")
        )))
                .isInstanceOf(PaymentAmountMismatchException.class)
                .hasMessageContaining("Payment amount 6000.00 must equal sale total 7000.00");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsCashTenderedBelowAmount() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(paymentRepository.existsBySaleId(50L)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CASH,
                new BigDecimal("7000.00"),
                new BigDecimal("5000.00")
        )))
                .isInstanceOf(InsufficientCashTenderedException.class)
                .hasMessageContaining("Cash tendered 5000.00 is less than payment amount 7000.00");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingCashTenderedForCashPayment() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(paymentRepository.existsBySaleId(50L)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CASH,
                new BigDecimal("7000.00"),
                null
        )))
                .isInstanceOf(InsufficientCashTenderedException.class)
                .hasMessageContaining("Cash tendered is required for cash payments");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicatePaymentForSale() {
        SaleEntity sale = sale("7000.00");
        when(saleRepository.findById(50L)).thenReturn(Optional.of(sale));
        when(paymentRepository.existsBySaleId(50L)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.create(new PaymentCreateRequest(
                50L,
                PaymentMethod.CARD,
                new BigDecimal("7000.00"),
                null
        )))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("Payment already exists for sale id: 50");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingSale() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.create(new PaymentCreateRequest(
                99L,
                PaymentMethod.CARD,
                new BigDecimal("7000.00"),
                null
        )))
                .isInstanceOf(SaleNotFoundException.class)
                .hasMessageContaining("Sale not found with id: 99");

        verify(paymentRepository, never()).save(any());
    }

    private static SaleEntity sale(String totalAmount) {
        BigDecimal unitPrice = new BigDecimal(totalAmount).divide(BigDecimal.valueOf(2));
        ProductEntity product = product(10L, unitPrice.toPlainString());
        SaleItemEntity item = ReflectionTestUtils.invokeMethod(SaleItemEntity.class, "create", product, 2);
        SaleEntity sale = SaleEntity.complete(cashier(), List.of(item));
        ReflectionTestUtils.setField(sale, "id", 50L);
        ReflectionTestUtils.setField(sale, "saleNumber", "SALE-001");
        return sale;
    }

    private static ProductEntity product(Long id, String price) {
        CategoryEntity category = new CategoryEntity("Beverages", null);
        ReflectionTestUtils.setField(category, "id", 1L);
        ProductEntity product = ProductEntity.create(new ProductDetails(
                category,
                "SKU-001",
                null,
                "Mineral Water",
                null,
                new BigDecimal(price),
                true
        ));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private static UserEntity cashier() {
        UserEntity user = new UserEntity("cashier@example.com", "Cashier One", "hash", UserRole.CASHIER, true);
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }
}
