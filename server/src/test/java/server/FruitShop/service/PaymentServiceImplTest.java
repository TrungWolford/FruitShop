package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Payment.PaymentRequest;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.entity.Payment;
import server.FruitShop.repository.PaymentRepository;
import server.FruitShop.service.Impl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Payment Service")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(0);
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setPaymentDate(new Date());
        testPayment.setTransactionId("TXN123");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Test 1: Lấy payment theo ID - Thành công")
    void testGetByPaymentId_Success() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));

        PaymentResponse result = paymentService.getByPaymentId("pay-001");

        assertNotNull(result);
        assertEquals("pay-001", result.getPaymentId());
        verify(paymentRepository, times(1)).findById("pay-001");
    }

    @Test
    @DisplayName("Test 2: Lấy payment theo ID - Không tìm thấy")
    void testGetByPaymentId_NotFound() {
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            paymentService.getByPaymentId("invalid-id");
        });
        
        verify(paymentRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 3: Tạo payment - Thành công")
    void testCreatePayment_Success() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("BANK_TRANSFER");
        request.setPaymentStatus(0);
        request.setAmount(BigDecimal.valueOf(200000));
        request.setTransactionId("TXN456");

        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.createPayment(request);

        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Test 4: Tạo payment - Thiếu payment method")
    void testCreatePayment_MissingMethod() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("");
        request.setAmount(BigDecimal.valueOf(100000));
        request.setPaymentStatus(0);

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(request);
        });
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Test 5: Cập nhật payment status - Thành công")
    void testUpdatePaymentStatus_Success() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.updatePaymentStatus("pay-001", 1);

        assertNotNull(result);
        verify(paymentRepository, times(1)).findById("pay-001");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Test 6: Cập nhật payment status - Invalid status")
    void testUpdatePaymentStatus_InvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.updatePaymentStatus("pay-001", 99);
        });
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Test 7: Lấy payments theo status - Thành công")
    void testGetPaymentsByStatus_Success() {
        List<Payment> payments = List.of(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());
        
        when(paymentRepository.findByPaymentStatus(0, pageable)).thenReturn(paymentPage);

        Page<PaymentResponse> result = paymentService.getPaymentsByStatus(0, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentRepository, times(1)).findByPaymentStatus(0, pageable);
    }

    @Test
    @DisplayName("Test 8: Lấy payment theo transaction ID - Thành công")
    void testGetPaymentByTransactionId_Success() {
        when(paymentRepository.findByTransactionId("TXN123")).thenReturn(Optional.of(testPayment));

        PaymentResponse result = paymentService.getPaymentByTransactionId("TXN123");

        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionId());
        verify(paymentRepository, times(1)).findByTransactionId("TXN123");
    }

    @Test
    @DisplayName("Test 9: Lấy tất cả payments - Thành công")
    void testGetAllPayment_Success() {
        List<Payment> payments = List.of(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());
        
        when(paymentRepository.findAll(pageable)).thenReturn(paymentPage);

        Page<PaymentResponse> result = paymentService.getAllPayment(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test 10: Cập nhật payment - Thành công")
    void testUpdatePayment_Success() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOMO");
        request.setPaymentStatus(1);
        request.setAmount(BigDecimal.valueOf(150000));

        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.updatePayment("pay-001", request);

        assertNotNull(result);
        verify(paymentRepository, times(1)).findById("pay-001");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
