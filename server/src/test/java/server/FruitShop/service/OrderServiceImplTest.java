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
import server.FruitShop.dto.request.Order.UpdateOrderRequest;
import server.FruitShop.dto.response.Order.OrderItemResponse;
import server.FruitShop.dto.response.Order.OrderResponse;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;
import server.FruitShop.service.Impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho OrderService
 * Class này test tất cả các chức năng liên quan đến quản lý đơn hàng
 * bao gồm: tạo, xem, cập nhật, hủy, xác nhận và theo dõi đơn hàng
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Order Service")
class OrderServiceImplTest {

    @Mock // Mock repository quản lý đơn hàng
    private OrderRepository orderRepository;

    @Mock // Mock repository quản lý chi tiết đơn hàng
    private OrderItemRepository orderItemRepository;

    @Mock // Mock repository quản lý tài khoản
    private AccountRepository accountRepository;

    @Mock // Mock repository quản lý sản phẩm
    private ProductRepository productRepository;

    @Mock // Mock repository quản lý vận chuyển
    private ShippingRepository shippingRepository;

    @Mock // Mock repository quản lý thanh toán
    private PaymentRepository paymentRepository;

    @InjectMocks // Inject tất cả mock vào service
    private OrderServiceImpl orderService;

    // Các entity mẫu để test
    private Order testOrder;
    private Account testAccount;
    private Payment testPayment;
    private Shipping testShipping;
    private Product testProduct;
    private OrderItem testOrderItem;
    private Pageable pageable;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo đầy đủ các entity liên quan: Account, Payment, Shipping, Product, Order, OrderItem
     */
    @BeforeEach
    void setUp() {
        // Tạo account test
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Test User");

        // Tạo payment test (COD - trả tiền khi nhận hàng)
        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(0); // 0 = Chưa thanh toán
        testPayment.setAmount(BigDecimal.valueOf(100000));

        // Tạo shipping test
        testShipping = new Shipping();
        testShipping.setShippingId("ship-001");
        testShipping.setReceiverName("Receiver");
        testShipping.setReceiverAddress("123 Street");
        testShipping.setStatus(1); // 1 = Chờ xử lý

        // Tạo product test
        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài");
        testProduct.setPrice(50000);
        testProduct.setStock(100); // 100 sản phẩm trong kho

        // Tạo order test với status = 1 (Chờ xác nhận)
        testOrder = new Order();
        testOrder.setOrderId("order-001");
        testOrder.setAccount(testAccount);
        testOrder.setPayment(testPayment);
        testOrder.setStatus(1); // 1 = Chờ xác nhận
        testOrder.setTotalAmount(100000);
        testOrder.setCreatedAt(new Date());
        testOrder.setOrderItems(new ArrayList<>());

        // Tạo order item test (2 sản phẩm Xoài)
        testOrderItem = new OrderItem();
        testOrderItem.setOrderDetailId("item-001");
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(50000);

        // Pageable cho phân trang
        pageable = PageRequest.of(0, 10);
    }

    /**
     * Test case 1: Kiểm tra lấy order theo ID
     * Kịch bản: Tìm order với ID tồn tại
     * Kết quả mong đợi: Trả về OrderResponse với đầy đủ thông tin
     */
    @Test
    @DisplayName("Test 1: Lấy order theo ID - Thành công")
    void testGetOrderById_Success() {
        // ARRANGE - Mock repository trả về order
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));

        // ACT - Gọi service để lấy order
        OrderResponse result = orderService.getOrderById("order-001");

        // ASSERT - Verify thông tin order
        assertNotNull(result);
        assertEquals("order-001", result.getOrderId()); // ID đúng
        assertEquals(1, result.getStatus()); // Status = Chờ xác nhận
        verify(orderRepository, times(1)).findById("order-001");
    }

    /**
     * Test case 2: Kiểm tra lấy order với ID không tồn tại
     * Kịch bản: Tìm order với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException
     */
    @Test
    @DisplayName("Test 2: Lấy order theo ID - Không tìm thấy")
    void testGetOrderById_NotFound() {
        // ARRANGE - Mock repository trả về empty
        when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository, times(1)).findById("invalid-id");
    }

    /**
     * Test case 3: Kiểm tra lấy danh sách orders theo account ID
     * Kịch bản: Lấy tất cả orders của một khách hàng cụ thể
     * Kết quả mong đợi: Trả về danh sách orders của khách hàng đó
     */
    @Test
    @DisplayName("Test 3: Lấy orders theo account ID - Thành công")
    void testGetOrdersByAccountId_Success() {
        // ARRANGE - Mock repository trả về danh sách orders
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByAccountAccountId("acc-001")).thenReturn(orders);

        // ACT - Gọi service để lấy orders
        List<OrderResponse> result = orderService.getOrdersByAccountId("acc-001");

        // ASSERT - Verify danh sách orders
        assertNotNull(result);
        assertEquals(1, result.size()); // 1 order được tìm thấy
        verify(orderRepository, times(1)).findByAccountAccountId("acc-001");
    }

    /**
     * Test case 4: Kiểm tra lấy tất cả orders với phân trang
     * Kịch bản: Lấy danh sách tất cả orders trong hệ thống
     * Kết quả mong đợi: Trả về Page chứa danh sách orders
     */
    @Test
    @DisplayName("Test 4: Lấy tất cả orders - Thành công")
    void testGetAllOrders_Success() {
        // ARRANGE - Tạo page chứa orders
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // ACT - Gọi service để lấy tất cả orders
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        // ASSERT - Verify page result
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    /**
     * Test case 5: Kiểm tra cập nhật trạng thái order
     * Kịch bản: Cập nhật status từ 1 (Chờ xác nhận) sang 2 (Đã xác nhận)
     * Kết quả mong đợi: Order được cập nhật status mới
     */
    @Test
    @DisplayName("Test 5: Cập nhật order status - Thành công")
    void testUpdateOrderStatus_Success() {
        // ARRANGE - Mock repository tìm và lưu order
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // ACT - Gọi service để update status
        OrderResponse result = orderService.updateOrderStatus("order-001", 2);

        // ASSERT - Verify status đã được cập nhật
        assertNotNull(result);
        assertEquals(2, testOrder.getStatus()); // Status đã chuyển sang 2
        verify(orderRepository, times(1)).save(testOrder);
    }

    /**
     * Test case 6: Kiểm tra cập nhật thông tin order
     * Kịch bản: Cập nhật status và payment ID của order
     * Kết quả mong đợi: Order được cập nhật với thông tin mới
     */
    @Test
    @DisplayName("Test 6: Update order - Thành công")
    void testUpdateOrder_Success() {
        // ARRANGE - Tạo request với thông tin cập nhật
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(2);
        request.setPaymentId("pay-001");

        // Mock repository tìm order và payment
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // ACT - Gọi service để update order
        OrderResponse result = orderService.updateOrder("order-001", request);

        // ASSERT - Verify order đã được cập nhật
        assertNotNull(result);
        verify(orderRepository, times(1)).save(testOrder);
    }

    /**
     * Test case 7: Kiểm tra hủy đơn hàng
     * Kịch bản: Hủy order đang ở trạng thái "Chờ xác nhận"
     * Kết quả mong đợi: Order status = 0 (Đã hủy), stock sản phẩm được hoàn lại
     */
    @Test
    @DisplayName("Test 7: Hủy order - Thành công")
    void testCancelOrder_Success() {
        // ARRANGE - Chuẩn bị order với items
        testOrder.setStatus(1); // Chờ xác nhận - có thể hủy
        List<OrderItem> items = List.of(testOrderItem);
        testOrder.setOrderItems(items);

        // Mock các repository cần thiết
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT - Gọi service để hủy order
        OrderResponse result = orderService.cancelOrder("order-001");

        // ASSERT - Verify order đã hủy và stock được restore
        assertNotNull(result);
        assertEquals(0, testOrder.getStatus()); // Status = Đã hủy
        assertEquals(102, testProduct.getStock()); // Stock restored: 100 + 2 = 102
        verify(orderRepository, times(1)).save(testOrder);
        verify(productRepository, times(1)).save(testProduct); // Stock được cập nhật
    }

    /**
     * Test case 8: Kiểm tra hủy order với status không hợp lệ
     * Kịch bản: Cố gắng hủy order đang trong quá trình giao hàng (status = 3)
     * Kết quả mong đợi: Throw RuntimeException, không cho phép hủy
     */
    @Test
    @DisplayName("Test 8: Hủy order - Status không hợp lệ")
    void testCancelOrder_InvalidStatus() {
        // ARRANGE - Order đang giao hàng, không thể hủy
        testOrder.setStatus(3); // Đang giao - không thể hủy
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder("order-001");
        });

        // Verify message và order không được save
        assertTrue(exception.getMessage().contains("Can only cancel"));
        verify(orderRepository, never()).save(any(Order.class)); // Không cập nhật
    }

    /**
     * Test case 9: Kiểm tra xác nhận đơn hàng
     * Kịch bản: Admin xác nhận order đang chờ xác nhận
     * Kết quả mong đợi: Order status chuyển từ 1 sang 2, shipping status cũng cập nhật
     */
    @Test
    @DisplayName("Test 9: Xác nhận order - Thành công")
    void testConfirmOrder_Success() {
        // ARRANGE - Order đang chờ xác nhận
        testOrder.setStatus(1); // Chờ xác nhận
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT - Admin xác nhận order
        OrderResponse result = orderService.confirmOrder("order-001");

        // ASSERT - Verify order và shipping được cập nhật
        assertNotNull(result);
        assertEquals(2, testOrder.getStatus()); // Status = Đã xác nhận
        assertEquals(2, testShipping.getStatus()); // Shipping cũng được xác nhận
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
    }

    /**
     * Test case 10: Kiểm tra bắt đầu giao hàng
     * Kịch bản: Chuyển order từ "Đã xác nhận" sang "Đang giao"
     * Kết quả mong đợi: Order và shipping status chuyển sang 3 (Đang giao)
     */
    @Test
    @DisplayName("Test 10: Bắt đầu giao hàng - Thành công")
    void testStartDelivery_Success() {
        // ARRANGE - Order đã được xác nhận
        testOrder.setStatus(2); // Đã xác nhận
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT - Bắt đầu giao hàng
        OrderResponse result = orderService.startDelivery("order-001");

        // ASSERT - Verify status chuyển sang đang giao
        assertNotNull(result);
        assertEquals(3, testOrder.getStatus()); // Status = Đang giao
        assertEquals(3, testShipping.getStatus()); // Shipping đang giao
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
    }

    /**
     * Test case 11: Kiểm tra hoàn thành đơn hàng
     * Kịch bản: Giao hàng thành công, chuyển sang trạng thái hoàn thành
     * Kết quả mong đợi: Order status = 4, shipping = 4, payment status = 1 (Đã thanh toán)
     */
    @Test
    @DisplayName("Test 11: Hoàn thành order - Thành công")
    void testCompleteOrder_Success() {
        // ARRANGE - Order đang giao hàng
        testOrder.setStatus(3); // Đang giao
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // ACT - Hoàn thành đơn hàng
        OrderResponse result = orderService.completeOrder("order-001");

        // ASSERT - Verify tất cả status được cập nhật
        assertNotNull(result);
        assertEquals(4, testOrder.getStatus()); // Status = Giao thành công
        assertEquals(4, testShipping.getStatus()); // Shipping hoàn thành
        assertEquals(1, testPayment.getPaymentStatus()); // Payment = Đã thanh toán
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
        verify(paymentRepository, times(1)).save(testPayment); // Payment được cập nhật
    }

    /**
     * Test case 12: Kiểm tra xóa đơn hàng
     * Kịch bản: Xóa order đã bị hủy khỏi hệ thống
     * Kết quả mong đợi: Order được xóa, stock không restore (đã restore khi cancel)
     */
    @Test
    @DisplayName("Test 12: Xóa order - Thành công")
    void testDeleteOrder_Success() {
        // ARRANGE - Order đã bị hủy trước đó
        testOrder.setStatus(0); // Đã hủy
        List<OrderItem> items = List.of(testOrderItem);
        testOrder.setOrderItems(items);

        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById("order-001");

        // ACT - Xóa order khỏi database
        orderService.deleteOrder("order-001");

        // ASSERT - Verify order được xóa
        verify(orderRepository, times(1)).findById("order-001");
        verify(orderRepository, times(1)).deleteById("order-001");
        // Stock không restore vì order đã cancelled trước đó (status = 0)
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Test case 13: Kiểm tra lọc orders theo trạng thái
     * Kịch bản: Lấy danh sách orders có status = 1 (Chờ xác nhận)
     * Kết quả mong đợi: Trả về Page chứa các orders có status tương ứng
     */
    @Test
    @DisplayName("Test 13: Lấy orders theo status - Thành công")
    void testGetOrdersByStatus_Success() {
        // ARRANGE - Mock repository trả về orders có status = 1
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findByStatus(1, pageable)).thenReturn(orderPage);

        // ACT - Lọc orders theo status
        Page<OrderResponse> result = orderService.getOrdersByStatus(1, pageable);

        // ASSERT - Verify kết quả lọc
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByStatus(1, pageable);
    }

    /**
     * Test case 14: Kiểm tra lấy chi tiết items của order
     * Kịch bản: Lấy danh sách tất cả sản phẩm trong một order
     * Kết quả mong đợi: Trả về danh sách OrderItemResponse
     */
    @Test
    @DisplayName("Test 14: Lấy order items theo order ID - Thành công")
    void testGetOrderDetailsByOrderId_Success() {
        // ARRANGE - Mock repository trả về order và items
        List<OrderItem> items = List.of(testOrderItem);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderOrderId("order-001")).thenReturn(items);

        // ACT - Lấy chi tiết items của order
        List<OrderItemResponse> result = orderService.getOrderDetailsByOrderId("order-001");

        // ASSERT - Verify danh sách items
        assertNotNull(result);
        assertEquals(1, result.size()); // 1 item trong order
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-001");
    }

    /**
     * Test case 15: Kiểm tra filter orders theo status
     * Kịch bản: Lọc và phân trang orders theo trạng thái cụ thể
     * Kết quả mong đợi: Trả về Page chứa orders đã được lọc
     */
    @Test
    @DisplayName("Test 15: Filter orders theo status - Thành công")
    void testFilterOrdersByStatus_Success() {
        // ARRANGE - Mock repository với filter
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findByStatus(1, pageable)).thenReturn(orderPage);

        // ACT - Filter orders theo status = 1
        Page<OrderResponse> result = orderService.filterOrdersByStatus(1, pageable);

        // ASSERT - Verify kết quả filter
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByStatus(1, pageable);
    }
}
