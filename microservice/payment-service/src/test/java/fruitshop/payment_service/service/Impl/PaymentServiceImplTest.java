package fruitshop.payment_service.service.Impl;

import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.entity.Payment;
import fruitshop.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPayment_invalidAmount_throwsIllegalArgument() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("o-1");
        request.setPaymentMethod("COD");
        request.setPaymentStatus(0);
        request.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_completed_publishesCompletedEvent() {
        Payment payment = new Payment();
        payment.setPaymentId("pay-1");
        payment.setOrderId("o-1");
        payment.setTransactionId("tx-1");
        payment.setAmount(new BigDecimal("120000"));
        payment.setPaymentDate(new Date());
        payment.setPaymentStatus(0);

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.updatePaymentStatus("pay-1", 1);

        assertEquals(1, response.getPaymentStatus());
        verify(streamBridge).send(eq("paymentCompletedSupplier-out-0"), any());
    }

    @Test
    void getPaymentsByStatus_outOfRange_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.getPaymentsByStatus(9, PageRequest.of(0, 10)));
    }

    @Test
    void getPaymentsByStatus_validStatus_returnsPayments() {
        Payment payment = new Payment();
        payment.setPaymentId("pay-1");
        payment.setPaymentStatus(1);
        
        var pageable = PageRequest.of(0, 10);
        when(paymentRepository.findByPaymentStatus(1, pageable))
            .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(payment), pageable, 1));

        var result = paymentService.getPaymentsByStatus(1, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void createPayment_validRequest_savesPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("o-1");
        request.setPaymentMethod("CARD");
        request.setPaymentStatus(0);
        request.setAmount(new BigDecimal("250000"));

        Payment saved = new Payment();
        saved.setPaymentId("pay-2");
        saved.setOrderId("o-1");
        saved.setAmount(new BigDecimal("250000"));
        saved.setPaymentStatus(0);
        saved.setPaymentMethod("CARD");

        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        var response = paymentService.createPayment(request);

        assertEquals("pay-2", response.getPaymentId());
        verify(paymentRepository).save(any(Payment.class));
    }
}
