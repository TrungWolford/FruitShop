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

/**
 * Unit Test cho PaymentService
 * Class này test tất cả các chức năng liên quan đến quản lý thanh toán
 * bao gồm: tạo, xem, cập nhật payment và kiểm tra trạng thái thanh toán
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Payment Service")
class PaymentServiceImplTest {

    @Mock // Mock repository quản lý thanh toán
    private PaymentRepository paymentRepository;

    @InjectMocks // Inject mock vào service
    private PaymentServiceImpl paymentService;

    // Các entity mẫu để test
    private Payment testPayment;
    private Pageable pageable;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo payment mẫu với method COD và các thông tin thanh toán
     */
    @BeforeEach
    void setUp() {
        // Tạo payment test với phương thức COD (trả tiền khi nhận hàng)
        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD"); // Cash On Delivery
        testPayment.setPaymentStatus(0); // 0 = Chưa thanh toán
        testPayment.setAmount(BigDecimal.valueOf(100000)); // 100,000 VND
        testPayment.setPaymentDate(new Date());
        testPayment.setTransactionId("TXN123"); // Mã giao dịch

        // Pageable cho phân trang
        pageable = PageRequest.of(0, 10);
    }

    /**
     * Test case 1: Kiểm tra lấy payment theo ID
     * Kịch bản: Tìm payment với ID tồn tại
     * Kết quả mong đợi: Trả về PaymentResponse với đầy đủ thông tin
     */
    @Test
    @DisplayName("Test 1: Lấy payment theo ID - Thành công")
    void testGetByPaymentId_Success() {
        // ARRANGE - Mock repository trả về payment
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));

        // ACT - Gọi service để lấy payment
        PaymentResponse result = paymentService.getByPaymentId("pay-001");

        // ASSERT - Verify thông tin payment
        assertNotNull(result);
        assertEquals("pay-001", result.getPaymentId()); // ID đúng
        verify(paymentRepository, times(1)).findById("pay-001");
    }

    /**
     * Test case 2: Kiểm tra lấy payment với ID không tồn tại
     * Kịch bản: Tìm payment với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException
     */
    @Test
    @DisplayName("Test 2: Lấy payment theo ID - Không tìm thấy")
    void testGetByPaymentId_NotFound() {
        // ARRANGE - Mock repository trả về empty
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        assertThrows(RuntimeException.class, () -> {
            paymentService.getByPaymentId("invalid-id");
        });
        
        verify(paymentRepository, times(1)).findById("invalid-id");
    }

    /**
     * Test case 3: Kiểm tra tạo payment mới
     * Kịch bản: Tạo payment với phương thức chuyển khoản ngân hàng
     * Kết quả mong đợi: Payment được tạo và lưu vào database
     */
    @Test
    @DisplayName("Test 3: Tạo payment - Thành công")
    void testCreatePayment_Success() {
        // ARRANGE - Tạo request với thông tin payment
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("BANK_TRANSFER"); // Chuyển khoản
        request.setPaymentStatus(0); // Chưa thanh toán
        request.setAmount(BigDecimal.valueOf(200000)); // 200,000 VND
        request.setTransactionId("TXN456");

        // Mock repository lưu payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // ACT - Gọi service để tạo payment
        PaymentResponse result = paymentService.createPayment(request);

        // ASSERT - Verify payment được tạo
        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    /**
     * Test case 4: Kiểm tra tạo payment với dữ liệu không hợp lệ
     * Kịch bản: Tạo payment nhưng thiếu payment method (bắt buộc)
     * Kết quả mong đợi: Throw IllegalArgumentException, không lưu vào database
     */
    @Test
    @DisplayName("Test 4: Tạo payment - Thiếu payment method")
    void testCreatePayment_MissingMethod() {
        // ARRANGE - Request thiếu payment method
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod(""); // Rỗng - không hợp lệ
        request.setAmount(BigDecimal.valueOf(100000));
        request.setPaymentStatus(0);

        // ACT & ASSERT - Verify validation exception
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(request);
        });
        
        // Verify không có payment nào được lưu
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    /**
     * Test case 5: Kiểm tra cập nhật trạng thái thanh toán
     * Kịch bản: Cập nhật status từ 0 (Chưa thanh toán) sang 1 (Đã thanh toán)
     * Kết quả mong đợi: Payment status được cập nhật thành công
     */
    @Test
    @DisplayName("Test 5: Cập nhật payment status - Thành công")
    void testUpdatePaymentStatus_Success() {
        // ARRANGE - Mock repository tìm và lưu payment
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // ACT - Cập nhật status sang 1 (Đã thanh toán)
        PaymentResponse result = paymentService.updatePaymentStatus("pay-001", 1);

        // ASSERT - Verify payment được cập nhật
        assertNotNull(result);
        verify(paymentRepository, times(1)).findById("pay-001");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    /**
     * Test case 6: Kiểm tra cập nhật với status không hợp lệ
     * Kịch bản: Cố gắng cập nhật status với giá trị không hợp lệ (99)
     * Kết quả mong đợi: Throw IllegalArgumentException, không cập nhật
     */
    @Test
    @DisplayName("Test 6: Cập nhật payment status - Invalid status")
    void testUpdatePaymentStatus_InvalidStatus() {
        // ACT & ASSERT - Verify validation exception với status = 99 (không hợp lệ)
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.updatePaymentStatus("pay-001", 99);
        });
        
        // Verify không có update nào được thực hiện
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    /**
     * Test case 7: Kiểm tra lọc payments theo trạng thái
     * Kịch bản: Lấy danh sách payments có status = 0 (Chưa thanh toán)
     * Kết quả mong đợi: Trả về Page chứa các payments chưa thanh toán
     */
    @Test
    @DisplayName("Test 7: Lấy payments theo status - Thành công")
    void testGetPaymentsByStatus_Success() {
        // ARRANGE - Mock repository trả về payments có status = 0
        List<Payment> payments = List.of(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());
        
        when(paymentRepository.findByPaymentStatus(0, pageable)).thenReturn(paymentPage);

        // ACT - Lọc payments chưa thanh toán
        Page<PaymentResponse> result = paymentService.getPaymentsByStatus(0, pageable);

        // ASSERT - Verify kết quả lọc
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // 1 payment chưa thanh toán
        verify(paymentRepository, times(1)).findByPaymentStatus(0, pageable);
    }

    /**
     * Test case 8: Kiểm tra tìm payment theo mã giao dịch
     * Kịch bản: Tìm payment với transaction ID tồn tại
     * Kết quả mong đợi: Trả về payment có transaction ID tương ứng
     */
    @Test
    @DisplayName("Test 8: Lấy payment theo transaction ID - Thành công")
    void testGetPaymentByTransactionId_Success() {
        // ARRANGE - Mock repository tìm theo transaction ID
        when(paymentRepository.findByTransactionId("TXN123")).thenReturn(Optional.of(testPayment));

        // ACT - Tìm payment theo mã giao dịch
        PaymentResponse result = paymentService.getPaymentByTransactionId("TXN123");

        // ASSERT - Verify payment được tìm thấy
        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionId()); // Transaction ID đúng
        verify(paymentRepository, times(1)).findByTransactionId("TXN123");
    }

    /**
     * Test case 9: Kiểm tra lấy tất cả payments với phân trang
     * Kịch bản: Lấy danh sách tất cả payments trong hệ thống
     * Kết quả mong đợi: Trả về Page chứa danh sách payments
     */
    @Test
    @DisplayName("Test 9: Lấy tất cả payments - Thành công")
    void testGetAllPayment_Success() {
        // ARRANGE - Tạo page chứa payments
        List<Payment> payments = List.of(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());
        
        when(paymentRepository.findAll(pageable)).thenReturn(paymentPage);

        // ACT - Lấy tất cả payments
        Page<PaymentResponse> result = paymentService.getAllPayment(pageable);

        // ASSERT - Verify page result
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentRepository, times(1)).findAll(pageable);
    }

    /**
     * Test case 10: Kiểm tra cập nhật thông tin payment
     * Kịch bản: Cập nhật payment method và amount của payment
     * Kết quả mong đợi: Payment được cập nhật với thông tin mới
     */
    @Test
    @DisplayName("Test 10: Cập nhật payment - Thành công")
    void testUpdatePayment_Success() {
        // ARRANGE - Tạo request với thông tin cập nhật
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("MOMO"); // Đổi sang ví MoMo
        request.setPaymentStatus(1); // Đã thanh toán
        request.setAmount(BigDecimal.valueOf(150000)); // Cập nhật số tiền

        // Mock repository tìm và lưu payment
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // ACT - Cập nhật payment
        PaymentResponse result = paymentService.updatePayment("pay-001", request);

        // ASSERT - Verify payment đã được cập nhật
        assertNotNull(result);
        verify(paymentRepository, times(1)).findById("pay-001");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
