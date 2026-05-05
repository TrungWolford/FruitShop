package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.request.Refund.UpdateRefundStatusRequest;
import server.FruitShop.dto.response.Refund.RefundResponse;
import server.FruitShop.entity.*;
import server.FruitShop.repository.OrderItemRepository;
import server.FruitShop.repository.OrderRepository;
import server.FruitShop.repository.PaymentRepository;
import server.FruitShop.repository.RefundRepository;
import server.FruitShop.service.Impl.RefundServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho RefundService
 * Class này test tất cả các chức năng liên quan đến hoàn tiền
 * bao gồm: tạo, duyệt, từ chối, hoàn thành và hủy yêu cầu hoàn tiền
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Refund Service")
class RefundServiceImplTest {

    @Mock // Mock repository quản lý hoàn tiền
    private RefundRepository refundRepository;

    @Mock // Mock repository quản lý đơn hàng
    private OrderRepository orderRepository;

    @Mock // Mock repository quản lý chi tiết đơn hàng
    private OrderItemRepository orderItemRepository;

    @Mock // Mock repository quản lý thanh toán
    private PaymentRepository paymentRepository;

    @InjectMocks // Inject tất cả mock vào service
    private RefundServiceImpl refundService;

    // Các entity mẫu để test
    private Refund testRefund;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Payment testPayment;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo đầy đủ các entity liên quan: Account, Order, Product, OrderItem, Payment, Refund
     */
    @BeforeEach
    void setUp() {
        // Tạo account test
        Account account = new Account();
        account.setAccountId("acc-001");
        account.setAccountName("Nguyễn Văn A");

        // Tạo order test
        testOrder = new Order();
        testOrder.setOrderId("order-001");
        testOrder.setAccount(account);
        testOrder.setTotalAmount(500000); // Tổng 500k

        // Tạo product test
        Product product = new Product();
        product.setProductId("prod-001");
        product.setProductName("Xoài Úc");

        // Tạo order item test (2 sản phẩm, mỗi cái 100k)
        testOrderItem = new OrderItem();
        testOrderItem.setOrderDetailId("item-001");
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(product);
        testOrderItem.setQuantity(2); // 2 sản phẩm
        testOrderItem.setUnitPrice(100000); // 100k/sản phẩm

        // Tạo payment test
        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD");
        testPayment.setAmount(BigDecimal.valueOf(500000)); // 500k

        // Tạo refund test với lý do sản phẩm hỏng
        testRefund = new Refund();
        testRefund.setRefundId("refund-001");
        testRefund.setOrder(testOrder);
        testRefund.setOrderItem(testOrderItem);
        testRefund.setReason("Sản phẩm bị hỏng"); // Lý do hoàn tiền
        testRefund.setRefundAmount(200000L); // Hoàn 200k
        testRefund.setRefundStatus("Chờ xác nhận"); // Status ban đầu
        testRefund.setOriginalPayment(testPayment); // Payment gốc
    }

    /**
     * Test 1: Lấy thông tin refund theo ID - Trường hợp thành công
     * Kịch bản: Tìm refund với ID hợp lệ đang tồn tại trong hệ thống
     * Kỳ vọng: Trả về RefundResponse chứa đầy đủ thông tin refund
     */
    @Test
    @DisplayName("Test 1: Lấy refund theo ID - Thành công")
    void testGetRefundById_Success() {
        // ARRANGE: Giả lập repository trả về refund khi tìm theo ID
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));

        // ACT: Gọi service để lấy refund
        RefundResponse result = refundService.getRefundById("refund-001");

        // ASSERT: Kiểm tra kết quả trả về
        assertNotNull(result); // Kết quả không null
        assertEquals("refund-001", result.getRefundId()); // ID đúng
        verify(refundRepository, times(1)).findById("refund-001"); // Gọi repo 1 lần
    }

    /**
     * Test 2: Lấy refund theo ID - Trường hợp không tìm thấy
     * Kịch bản: Tìm refund với ID không tồn tại trong hệ thống
     * Kỳ vọng: Ném RuntimeException vì không tìm thấy refund
     */
    @Test
    @DisplayName("Test 2: Lấy refund theo ID - Không tìm thấy")
    void testGetRefundById_NotFound() {
        // ARRANGE: Giả lập repository không tìm thấy refund
        when(refundRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        assertThrows(RuntimeException.class, () -> {
            refundService.getRefundById("invalid-id"); // Phải ném exception
        });
        
        verify(refundRepository, times(1)).findById("invalid-id"); // Đã gọi repo
    }

    /**
     * Test 3: Tạo yêu cầu refund mới - Trường hợp thành công
     * Kịch bản: Khách hàng yêu cầu hoàn tiền với order và order item hợp lệ
     * Kỳ vọng: Tạo refund thành công với status "Chờ xác nhận"
     */
    @Test
    @DisplayName("Test 3: Tạo refund - Thành công")
    void testCreateRefund_Success() {
        // ARRANGE: Chuẩn bị request tạo refund
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId("order-001"); // Order cần hoàn tiền
        request.setOrderItemId("item-001"); // Item cụ thể cần hoàn
        request.setReason("Sản phẩm không đúng mô tả"); // Lý do
        request.setRefundAmount(200000L); // Số tiền hoàn: 200k

        // Giả lập tìm thấy order và order item
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findById("item-001")).thenReturn(Optional.of(testOrderItem));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // ACT: Gọi service tạo refund
        RefundResponse result = refundService.createRefund(request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(orderRepository, times(1)).findById("order-001"); // Đã tìm order
        verify(refundRepository, times(1)).save(any(Refund.class)); // Đã lưu refund
    }

    /**
     * Test 4: Tạo refund - Trường hợp order không tồn tại
     * Kịch bản: Khách hàng yêu cầu hoàn tiền với order ID không hợp lệ
     * Kỳ vọng: Ném RuntimeException, không tạo refund trong DB
     */
    @Test
    @DisplayName("Test 4: Tạo refund - Order không tồn tại")
    void testCreateRefund_OrderNotFound() {
        // ARRANGE: Chuẩn bị request với order ID không tồn tại
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId("invalid-id"); // Order không tồn tại
        request.setReason("Test");
        request.setRefundAmount(100000L);

        // Giả lập không tìm thấy order
        when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        assertThrows(RuntimeException.class, () -> {
            refundService.createRefund(request); // Phải ném exception
        });
        
        verify(refundRepository, never()).save(any(Refund.class)); // Không lưu refund
    }

    /**
     * Test 5: Duyệt yêu cầu refund - Trường hợp thành công
     * Kịch bản: Admin duyệt yêu cầu hoàn tiền hợp lệ
     * Kỳ vọng: Cập nhật status sang "Đã duyệt", khách hàng sẽ được hoàn tiền
     */
    @Test
    @DisplayName("Test 5: Duyệt refund - Thành công")
    void testApproveRefund_Success() {
        // ARRANGE: Giả lập tìm thấy refund
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // ACT: Admin duyệt refund
        RefundResponse result = refundService.approveRefund("refund-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(refundRepository, times(1)).findById("refund-001"); // Đã tìm refund
        verify(refundRepository, times(1)).save(any(Refund.class)); // Đã cập nhật status
    }

    /**
     * Test 6: Từ chối yêu cầu refund - Trường hợp thành công
     * Kịch bản: Admin từ chối yêu cầu hoàn tiền vì lý do không hợp lệ
     * Kỳ vọng: Cập nhật status sang "Đã từ chối", không hoàn tiền
     */
    @Test
    @DisplayName("Test 6: Từ chối refund - Thành công")
    void testRejectRefund_Success() {
        // ARRANGE: Giả lập tìm thấy refund
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // ACT: Admin từ chối refund
        RefundResponse result = refundService.rejectRefund("refund-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(refundRepository, times(1)).findById("refund-001"); // Đã tìm refund
        verify(refundRepository, times(1)).save(any(Refund.class)); // Đã cập nhật status
    }

    /**
     * Test 7: Hoàn thành refund - Trường hợp thành công
     * Kịch bản: Sau khi duyệt, admin xác nhận đã hoàn tiền cho khách hàng
     * Kỳ vọng: Cập nhật status sang "Hoàn thành", kết thúc quy trình hoàn tiền
     */
    @Test
    @DisplayName("Test 7: Hoàn thành refund - Thành công")
    void testCompleteRefund_Success() {
        // ARRANGE: Giả lập tìm thấy refund
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // ACT: Admin xác nhận hoàn thành refund
        RefundResponse result = refundService.completeRefund("refund-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(refundRepository, times(1)).findById("refund-001"); // Đã tìm refund
        verify(refundRepository, times(1)).save(any(Refund.class)); // Đã cập nhật status
    }

    /**
     * Test 8: Hủy yêu cầu refund - Trường hợp thành công
     * Kịch bản: Khách hàng hoặc admin hủy yêu cầu hoàn tiền
     * Kỳ vọng: Xóa refund khỏi hệ thống, không còn tồn tại trong DB
     */
    @Test
    @DisplayName("Test 8: Hủy refund - Thành công")
    void testCancelRefund_Success() {
        // ARRANGE: Giả lập tìm thấy refund và cho phép xóa
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        doNothing().when(refundRepository).delete(testRefund);

        // ACT: Hủy refund
        refundService.cancelRefund("refund-001");

        // ASSERT: Kiểm tra đã xóa
        verify(refundRepository, times(1)).findById("refund-001"); // Đã tìm refund
        verify(refundRepository, times(1)).delete(testRefund); // Đã xóa khỏi DB
    }

    /**
     * Test 9: Cập nhật status của refund - Trường hợp thành công
     * Kịch bản: Admin thay đổi trạng thái refund (ví dụ: Chờ xác nhận → Đã duyệt)
     * Kỳ vọng: Status được cập nhật theo request, lưu vào DB thành công
     */
    @Test
    @DisplayName("Test 9: Cập nhật refund status - Thành công")
    void testUpdateRefundStatus_Success() {
        // ARRANGE: Chuẩn bị request cập nhật status
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setRefundStatus("Đã duyệt"); // Status mới

        // Giả lập tìm thấy refund
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        // ACT: Cập nhật status
        RefundResponse result = refundService.updateRefundStatus(request, "refund-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(refundRepository, times(1)).findById("refund-001"); // Đã tìm refund
        verify(refundRepository, times(1)).save(any(Refund.class)); // Đã lưu status mới
    }

    /**
     * Test 10: Đếm số lượng refunds đang chờ xử lý - Trường hợp thành công
     * Kịch bản: Admin muốn biết có bao nhiêu yêu cầu hoàn tiền chưa xử lý
     * Kỳ vọng: Trả về số lượng refunds có status "Chờ xác nhận"
     */
    @Test
    @DisplayName("Test 10: Đếm refunds đang chờ - Thành công")
    void testCountPendingRefunds_Success() {
        // ARRANGE: Giả lập có 5 refunds đang chờ
        when(refundRepository.countByRefundStatus("Chờ xác nhận")).thenReturn(5L);

        // ACT: Gọi service đếm refunds đang chờ
        long result = refundService.countPendingRefunds();

        // ASSERT: Kiểm tra kết quả
        assertEquals(5L, result); // Đúng 5 refunds đang chờ
        verify(refundRepository, times(1)).countByRefundStatus("Chờ xác nhận"); // Đã đếm theo status
    }
}
