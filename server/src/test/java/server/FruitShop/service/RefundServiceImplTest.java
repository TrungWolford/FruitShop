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

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Refund Service")
class RefundServiceImplTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RefundServiceImpl refundService;

    private Refund testRefund;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setAccountId("acc-001");
        account.setAccountName("Nguyễn Văn A");

        testOrder = new Order();
        testOrder.setOrderId("order-001");
        testOrder.setAccount(account);
        testOrder.setTotalAmount(500000);

        Product product = new Product();
        product.setProductId("prod-001");
        product.setProductName("Xoài Úc");

        testOrderItem = new OrderItem();
        testOrderItem.setOrderDetailId("item-001");
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(product);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(100000);

        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD");
        testPayment.setAmount(BigDecimal.valueOf(500000));

        testRefund = new Refund();
        testRefund.setRefundId("refund-001");
        testRefund.setOrder(testOrder);
        testRefund.setOrderItem(testOrderItem);
        testRefund.setReason("Sản phẩm bị hỏng");
        testRefund.setRefundAmount(200000L);
        testRefund.setRefundStatus("Chờ xác nhận");
        testRefund.setOriginalPayment(testPayment);
    }

    @Test
    @DisplayName("Test 1: Lấy refund theo ID - Thành công")
    void testGetRefundById_Success() {
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));

        RefundResponse result = refundService.getRefundById("refund-001");

        assertNotNull(result);
        assertEquals("refund-001", result.getRefundId());
        verify(refundRepository, times(1)).findById("refund-001");
    }

    @Test
    @DisplayName("Test 2: Lấy refund theo ID - Không tìm thấy")
    void testGetRefundById_NotFound() {
        when(refundRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            refundService.getRefundById("invalid-id");
        });
        
        verify(refundRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 3: Tạo refund - Thành công")
    void testCreateRefund_Success() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId("order-001");
        request.setOrderItemId("item-001");
        request.setReason("Sản phẩm không đúng mô tả");
        request.setRefundAmount(200000L);

        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findById("item-001")).thenReturn(Optional.of(testOrderItem));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        RefundResponse result = refundService.createRefund(request);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById("order-001");
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 4: Tạo refund - Order không tồn tại")
    void testCreateRefund_OrderNotFound() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId("invalid-id");
        request.setReason("Test");
        request.setRefundAmount(100000L);

        when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            refundService.createRefund(request);
        });
        
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 5: Duyệt refund - Thành công")
    void testApproveRefund_Success() {
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        RefundResponse result = refundService.approveRefund("refund-001");

        assertNotNull(result);
        verify(refundRepository, times(1)).findById("refund-001");
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 6: Từ chối refund - Thành công")
    void testRejectRefund_Success() {
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        RefundResponse result = refundService.rejectRefund("refund-001");

        assertNotNull(result);
        verify(refundRepository, times(1)).findById("refund-001");
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 7: Hoàn thành refund - Thành công")
    void testCompleteRefund_Success() {
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        RefundResponse result = refundService.completeRefund("refund-001");

        assertNotNull(result);
        verify(refundRepository, times(1)).findById("refund-001");
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 8: Hủy refund - Thành công")
    void testCancelRefund_Success() {
        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        doNothing().when(refundRepository).delete(testRefund);

        refundService.cancelRefund("refund-001");

        verify(refundRepository, times(1)).findById("refund-001");
        verify(refundRepository, times(1)).delete(testRefund);
    }

    @Test
    @DisplayName("Test 9: Cập nhật refund status - Thành công")
    void testUpdateRefundStatus_Success() {
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setRefundStatus("Đã duyệt");

        when(refundRepository.findById("refund-001")).thenReturn(Optional.of(testRefund));
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

        RefundResponse result = refundService.updateRefundStatus(request, "refund-001");

        assertNotNull(result);
        verify(refundRepository, times(1)).findById("refund-001");
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    @DisplayName("Test 10: Đếm refunds đang chờ - Thành công")
    void testCountPendingRefunds_Success() {
        when(refundRepository.countByRefundStatus("Chờ xác nhận")).thenReturn(5L);

        long result = refundService.countPendingRefunds();

        assertEquals(5L, result);
        verify(refundRepository, times(1)).countByRefundStatus("Chờ xác nhận");
    }
}
