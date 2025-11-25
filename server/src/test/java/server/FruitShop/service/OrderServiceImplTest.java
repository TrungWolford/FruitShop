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
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Order Service")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Account testAccount;
    private Payment testPayment;
    private Shipping testShipping;
    private Product testProduct;
    private OrderItem testOrderItem;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Test User");

        testPayment = new Payment();
        testPayment.setPaymentId("pay-001");
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(0);
        testPayment.setAmount(BigDecimal.valueOf(100000));

        testShipping = new Shipping();
        testShipping.setShippingId("ship-001");
        testShipping.setReceiverName("Receiver");
        testShipping.setReceiverAddress("123 Street");
        testShipping.setStatus(1);

        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài");
        testProduct.setPrice(50000);
        testProduct.setStock(100);

        testOrder = new Order();
        testOrder.setOrderId("order-001");
        testOrder.setAccount(testAccount);
        testOrder.setPayment(testPayment);
        testOrder.setStatus(1); // Chờ xác nhận
        testOrder.setTotalAmount(100000);
        testOrder.setCreatedAt(new Date());
        testOrder.setOrderItems(new ArrayList<>());

        testOrderItem = new OrderItem();
        testOrderItem.setOrderDetailId("item-001");
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(50000);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Test 1: Lấy order theo ID - Thành công")
    void testGetOrderById_Success() {
        // ARRANGE
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));

        // ACT
        OrderResponse result = orderService.getOrderById("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("order-001", result.getOrderId());
        assertEquals(1, result.getStatus());
        verify(orderRepository, times(1)).findById("order-001");
    }

    @Test
    @DisplayName("Test 2: Lấy order theo ID - Không tìm thấy")
    void testGetOrderById_NotFound() {
        // ARRANGE
        when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 3: Lấy orders theo account ID - Thành công")
    void testGetOrdersByAccountId_Success() {
        // ARRANGE
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByAccountAccountId("acc-001")).thenReturn(orders);

        // ACT
        List<OrderResponse> result = orderService.getOrdersByAccountId("acc-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByAccountAccountId("acc-001");
    }

    @Test
    @DisplayName("Test 4: Lấy tất cả orders - Thành công")
    void testGetAllOrders_Success() {
        // ARRANGE
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // ACT
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test 5: Cập nhật order status - Thành công")
    void testUpdateOrderStatus_Success() {
        // ARRANGE
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // ACT
        OrderResponse result = orderService.updateOrderStatus("order-001", 2);

        // ASSERT
        assertNotNull(result);
        assertEquals(2, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Test 6: Update order - Thành công")
    void testUpdateOrder_Success() {
        // ARRANGE
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(2);
        request.setPaymentId("pay-001");

        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(testPayment));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // ACT
        OrderResponse result = orderService.updateOrder("order-001", request);

        // ASSERT
        assertNotNull(result);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Test 7: Hủy order - Thành công")
    void testCancelOrder_Success() {
        // ARRANGE
        testOrder.setStatus(1); // Chờ xác nhận
        List<OrderItem> items = List.of(testOrderItem);
        testOrder.setOrderItems(items);

        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT
        OrderResponse result = orderService.cancelOrder("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(0, testOrder.getStatus()); // Đã hủy
        assertEquals(102, testProduct.getStock()); // Stock restored: 100 + 2
        verify(orderRepository, times(1)).save(testOrder);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Test 8: Hủy order - Status không hợp lệ")
    void testCancelOrder_InvalidStatus() {
        // ARRANGE
        testOrder.setStatus(3); // Đang giao - không thể hủy
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder("order-001");
        });

        assertTrue(exception.getMessage().contains("Can only cancel"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Test 9: Xác nhận order - Thành công")
    void testConfirmOrder_Success() {
        // ARRANGE
        testOrder.setStatus(1); // Chờ xác nhận
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT
        OrderResponse result = orderService.confirmOrder("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(2, testOrder.getStatus()); // Đã xác nhận
        assertEquals(2, testShipping.getStatus()); // Shipping cũng đã xác nhận
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
    }

    @Test
    @DisplayName("Test 10: Bắt đầu giao hàng - Thành công")
    void testStartDelivery_Success() {
        // ARRANGE
        testOrder.setStatus(2); // Đã xác nhận
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT
        OrderResponse result = orderService.startDelivery("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(3, testOrder.getStatus()); // Đang giao
        assertEquals(3, testShipping.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
    }

    @Test
    @DisplayName("Test 11: Hoàn thành order - Thành công")
    void testCompleteOrder_Success() {
        // ARRANGE
        testOrder.setStatus(3); // Đang giao
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(shippingRepository.findByOrderOrderId("order-001")).thenReturn(testShipping);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // ACT
        OrderResponse result = orderService.completeOrder("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(4, testOrder.getStatus()); // Giao thành công
        assertEquals(4, testShipping.getStatus());
        assertEquals(1, testPayment.getPaymentStatus()); // Payment completed
        verify(orderRepository, times(1)).save(testOrder);
        verify(shippingRepository, times(1)).save(testShipping);
        verify(paymentRepository, times(1)).save(testPayment);
    }

    @Test
    @DisplayName("Test 12: Xóa order - Thành công")
    void testDeleteOrder_Success() {
        // ARRANGE
        testOrder.setStatus(0); // Đã hủy
        List<OrderItem> items = List.of(testOrderItem);
        testOrder.setOrderItems(items);

        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById("order-001");

        // ACT
        orderService.deleteOrder("order-001");

        // ASSERT
        verify(orderRepository, times(1)).findById("order-001");
        verify(orderRepository, times(1)).deleteById("order-001");
        // Stock không restore vì order đã cancelled (status = 0)
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Test 13: Lấy orders theo status - Thành công")
    void testGetOrdersByStatus_Success() {
        // ARRANGE
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findByStatus(1, pageable)).thenReturn(orderPage);

        // ACT
        Page<OrderResponse> result = orderService.getOrdersByStatus(1, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByStatus(1, pageable);
    }

    @Test
    @DisplayName("Test 14: Lấy order items theo order ID - Thành công")
    void testGetOrderDetailsByOrderId_Success() {
        // ARRANGE
        List<OrderItem> items = List.of(testOrderItem);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderOrderId("order-001")).thenReturn(items);

        // ACT
        List<OrderItemResponse> result = orderService.getOrderDetailsByOrderId("order-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-001");
    }

    @Test
    @DisplayName("Test 15: Filter orders theo status - Thành công")
    void testFilterOrdersByStatus_Success() {
        // ARRANGE
        List<Order> orders = List.of(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findByStatus(1, pageable)).thenReturn(orderPage);

        // ACT
        Page<OrderResponse> result = orderService.filterOrdersByStatus(1, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByStatus(1, pageable);
    }
}
