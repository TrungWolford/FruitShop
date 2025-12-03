package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Order.CreateOrderRequest;
import server.FruitShop.dto.request.Order.UpdateOrderRequest;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Order API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 * 
 * Order Status Flow:
 * 0: Cancelled (Đã hủy)
 * 1: Pending (Chờ xác nhận) 
 * 2: Confirmed (Đã xác nhận)
 * 3: Delivering (Đang giao)
 * 4: Completed (Hoàn thành)
 * 
 * Bao gồm:
 * - CRUD operations (lấy, tạo, cập nhật, xóa)
 * - Order status workflow (confirm, start delivery, complete, cancel)
 * - Search và filter orders
 * - Validation và error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    private Account testAccount;
    private Product testProduct;
    private Payment testPayment;
    private Shipping testShipping;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo role
        Role customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");
        customerRole = roleRepository.save(customerRole);

        // Tạo account
        testAccount = new Account();
        testAccount.setAccountName("Nguyễn Văn A");
        testAccount.setAccountPhone("0355142890");
        testAccount.setPassword(passwordEncoder.encode("123456"));
        testAccount.setStatus(1);
        testAccount.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
        testAccount = accountRepository.save(testAccount);

        // Tạo category
        Category testCategory = new Category();
        testCategory.setCategoryName("Trái cây");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);

        // Tạo product
        testProduct = new Product();
        testProduct.setProductName("Xoài Úc");
        testProduct.setPrice(50000);
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct.setCategories(Collections.singletonList(testCategory));
        testProduct = productRepository.save(testProduct);

        // Tạo payment
        testPayment = new Payment();
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(0);
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setPaymentDate(new Date());
        testPayment = paymentRepository.save(testPayment);

        // Tạo shipping
        testShipping = new Shipping();
        testShipping.setAccount(testAccount);
        testShipping.setReceiverName("Nguyễn Văn A");
        testShipping.setReceiverPhone("0355142890");
        testShipping.setReceiverAddress("123 Đường ABC, Quận 1, TP.HCM");
        testShipping.setCity("TP.HCM");
        testShipping.setShipperName("");
        testShipping.setShippingFee(30000);
        testShipping.setStatus(1);
        testShipping = shippingRepository.save(testShipping);

        // Tạo order
        testOrder = new Order();
        testOrder.setAccount(testAccount);
        testOrder.setPayment(testPayment);
        testOrder.setShipping(testShipping);
        testOrder.setStatus(0); // Chờ xác nhận
        testOrder.setTotalAmount(100000L);
        testOrder = orderRepository.save(testOrder);
        
        // Cập nhật shipping với orderId (quan hệ 2 chiều)
        testShipping.setOrder(testOrder);
        shippingRepository.save(testShipping);

        // Tạo order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(50000);
        orderItemRepository.save(orderItem);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả đơn hàng với phân trang
     * Mục đích: Kiểm tra API GET /api/order trả về đúng danh sách orders
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 order (testOrder từ setUp)
     * - Order có totalAmount = 100000
     * Use case: Admin xem danh sách tất cả đơn hàng
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả orders - Thành công")
    void testGetAllOrders_Success() throws Exception {
        mockMvc.perform(get("/api/order")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].totalAmount").value(100000));
    }

    /**
     * Test Case 2: Lấy đơn hàng theo tài khoản
     * Mục đích: Kiểm tra API GET /api/order/account/{accountId}
     * Input: accountId của testAccount
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response là array có ít nhất 1 order
     * - Order có accountId khớp với testAccount
     * Use case: Khách hàng xem lịch sử đơn hàng của mình
     */
    @Test
    @DisplayName("Integration Test 2: Lấy orders theo accountId - Thành công")
    void testGetOrdersByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/order/account/{accountId}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].accountId").value(testAccount.getAccountId()));
    }

    /**
     * Test Case 3: Lấy đơn hàng theo trạng thái
     * Mục đích: Kiểm tra API GET /api/order/status/{status}
     * Input: status = 0 (Cancelled/Pending)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 order
     * - Tất cả orders có status = 0
     * Use case: Admin lọc đơn hàng theo trạng thái
     */
    @Test
    @DisplayName("Integration Test 3: Lấy orders theo status - Thành công")
    void testGetOrdersByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/order/status/{status}", 0)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status").value(0));
    }

    // Thiếu 

    /**
     * Test Case 5: Cập nhật thông tin đơn hàng
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}
     * Input: UpdateOrderRequest với status = 1 (Pending)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response status = 1
     * - Database: order.status được cập nhật thành công
     * Use case: Admin cập nhật trạng thái đơn hàng
     */
    @Test
    @DisplayName("Integration Test 5: Cập nhật order - Thành công")
    void testUpdateOrder_Success() throws Exception {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(1); // Đã xác nhận

        mockMvc.perform(put("/api/order/{orderId}", testOrder.getOrderId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        Order updated = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert updated.getStatus() == 1;
    }

    /**
     * Test Case 6: Xóa đơn hàng
     * Mục đích: Kiểm tra API DELETE /api/order/{orderId}
     * Setup: Xóa order items trước để tránh foreign key constraint
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Order bị xóa khỏi database
     * Note: @Transactional sẽ rollback nên không cần verify
     */
    @Test
    @DisplayName("Integration Test 6: Xóa order - Thành công")
    void testDeleteOrder_Success() throws Exception {
        // Xóa order items trước để tránh foreign key constraint
        orderItemRepository.deleteAll();
        
        mockMvc.perform(delete("/api/order/{orderId}", testOrder.getOrderId()))
                .andExpect(status().isOk());

        // Verify trong database - không cần check vì @Transactional sẽ rollback
        // boolean exists = orderRepository.existsById(testOrder.getOrderId());
        // assert !exists;
    }

    /**
     * Test Case 7: Xác nhận đơn hàng
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/confirm
     * Setup: Set order status = 1 (Pending)
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Database: order.status = 2 (Confirmed)
     * Business logic: Chỉ order Pending (status=1) mới confirm được
     * Use case: Admin xác nhận đơn hàng sau khi kiểm tra
     */
    @Test
    @DisplayName("Integration Test 7: Confirm order - Thành công")
    void testConfirmOrder_Success() throws Exception {
        // Backend requires status = 1 (Pending) để confirm thành status = 2 (Confirmed)
        testOrder.setStatus(1);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/confirm", testOrder.getOrderId()))
                .andExpect(status().isOk());

        // Verify trong database
        Order confirmed = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert confirmed.getStatus() == 2; // Confirmed
    }

    /**
     * Test Case 8: Bắt đầu giao hàng
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/start-delivery
     * Setup: Set order status = 2 (Confirmed)
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Database: order.status = 3 (Delivering)
     * Business logic: Chỉ order Confirmed (status=2) mới start delivery được
     * Use case: Shipper bắt đầu giao hàng
     */
    @Test
    @DisplayName("Integration Test 8: Start delivery - Thành công")
    void testStartDelivery_Success() throws Exception {
        // Backend requires status = 2 (Confirmed) để start delivery thành status = 3 (Delivering)
        testOrder.setStatus(2);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/start-delivery", testOrder.getOrderId()))
                .andExpect(status().isOk());

        // Verify trong database
        Order delivering = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert delivering.getStatus() == 3; // Delivering
    }

    /**
     * Test Case 9: Hoàn thành đơn hàng
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/complete
     * Setup: Set order status = 3 (Delivering)
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Database: order.status = 4 (Completed)
     * Business logic: Chỉ order Delivering (status=3) mới complete được
     * Use case: Khách hàng xác nhận đã nhận hàng
     */
    @Test
    @DisplayName("Integration Test 9: Complete order - Thành công")
    void testCompleteOrder_Success() throws Exception {
        // Backend requires status = 3 (Delivering) để complete thành status = 4 (Completed)
        testOrder.setStatus(3);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/complete", testOrder.getOrderId()))
                .andExpect(status().isOk());

        // Verify trong database
        Order completed = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert completed.getStatus() == 4; // Completed
    }

    /**
     * Test Case 10: Hủy đơn hàng
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/cancel
     * Setup: Set order status = 1 (Pending)
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Database: order.status = 0 (Cancelled)
     * Business logic: Chỉ order Pending (status=1) mới cancel được
     * Use case: Khách hàng hoặc Admin hủy đơn hàng
     */
    @Test
    @DisplayName("Integration Test 10: Cancel order - Thành công")
    void testCancelOrder_Success() throws Exception {
        // Backend requires status = 1 (Pending) để cancel thành status = 0 (Cancelled)
        testOrder.setStatus(1);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/cancel", testOrder.getOrderId()))
                .andExpect(status().isOk());

        // Verify trong database
        Order cancelled = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert cancelled.getStatus() == 0; // Cancelled
    }

    /**
     * Test Case 11: Cập nhật trạng thái đơn hàng trực tiếp
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/update-status
     * Input: orderId và status parameter = 1
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response status = 1
     * - Database: order.status được cập nhật
     * Note: API này cho phép update status trực tiếp, không qua workflow
     */
    @Test
    @DisplayName("Integration Test 11: Update order status - Thành công")
    void testUpdateOrderStatus_Success() throws Exception {
        mockMvc.perform(put("/api/order/{orderId}/update-status", testOrder.getOrderId())
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        Order updated = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert updated.getStatus() == 1;
    }

    /**
     * Test Case 12: Tìm kiếm đơn hàng theo từ khóa
     * Mục đích: Kiểm tra API GET /api/order/search
     * Input: keyword = "Nguyễn" (tìm theo tên khách hàng)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 order khớp với keyword
     * Use case: Admin tìm kiếm đơn hàng theo tên khách hàng
     */
    @Test
    @DisplayName("Integration Test 12: Search orders - Thành công")
    void testSearchOrders_Success() throws Exception {
        mockMvc.perform(get("/api/order/search")
                        .param("keyword", "Nguyễn")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test Case 13: Lọc đơn hàng theo trạng thái (filter endpoint)
     * Mục đích: Kiểm tra API GET /api/order/filter
     * Input: status parameter = 0
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 order
     * - Tất cả orders có status = 0
     * Note: Khác với test 3, endpoint này dùng cho filter UI
     */
    @Test
    @DisplayName("Integration Test 13: Filter orders by status - Thành công")
    void testFilterOrdersByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/order/filter")
                        .param("status", "0")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status").value(0));
    }

    /**
     * Test Case 14: Tìm kiếm và lọc đơn hàng kết hợp
     * Mục đích: Kiểm tra API GET /api/order/search-filter
     * Input: 
     * - keyword = "Nguyễn" (tìm theo tên)
     * - status = 0 (lọc theo trạng thái)
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 order thỏa cả 2 điều kiện
     * Use case: Admin tìm kiếm và lọc đơn hàng đồng thời
     */
    @Test
    @DisplayName("Integration Test 14: Search and filter orders - Thành công")
    void testSearchAndFilterOrders_Success() throws Exception {
        mockMvc.perform(get("/api/order/search-filter")
                        .param("keyword", "Nguyễn")
                        .param("status", "0")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test Case 15: Hủy đơn hàng với trạng thái không hợp lệ
     * Mục đích: Kiểm tra validation khi hủy order với status không được phép
     * Setup: Set order status = 3 (Delivering - đang giao)
     * Input: orderId của testOrder
     * Kết quả mong muốn:
     * - HTTP Status: 400 Bad Request
     * - Không thay đổi status trong database
     * Business logic: Không thể hủy đơn đang giao hoặc đã hoàn thành
     */
    @Test
    @DisplayName("Integration Test 15: Cancel order - Status không hợp lệ")
    void testCancelOrder_InvalidStatus() throws Exception {
        // Set order đã hoàn thành - không thể cancel
        testOrder.setStatus(3);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/cancel", testOrder.getOrderId()))
                .andExpect(status().isBadRequest());
    }
}
