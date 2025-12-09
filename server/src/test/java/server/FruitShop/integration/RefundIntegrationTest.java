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
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.request.Refund.UpdateRefundStatusRequest;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Refund API
 * Refund Status: Chờ xác nhận, Đã duyệt, Từ chối, Hoàn thành
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class RefundIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefundRepository refundRepository;

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
    private ObjectMapper objectMapper;

    private Refund testRefund;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Account testAccount;
    private Product testProduct;
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
        Payment testPayment = new Payment();
        testPayment.setPaymentMethod("COD");
        testPayment.setPaymentStatus(1);
        testPayment.setAmount(BigDecimal.valueOf(100000));
        testPayment.setPaymentDate(new Date());
        testPayment = paymentRepository.save(testPayment);

        // Tạo order
        testOrder = new Order();
        testOrder.setAccount(testAccount);
        testOrder.setPayment(testPayment);
        testOrder.setStatus(2); // Đang giao
        testOrder.setTotalAmount(100000L);
        testOrder = orderRepository.save(testOrder);

        // Tạo order item
        testOrderItem = new OrderItem();
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(50000);
        testOrderItem = orderItemRepository.save(testOrderItem);

        // Tạo refund
        testRefund = new Refund();
        testRefund.setOrder(testOrder);
        testRefund.setOrderItem(testOrderItem);
        testRefund.setReason("Sản phẩm không đúng mô tả");
        testRefund.setRefundAmount(100000L);
        testRefund.setRefundStatus("Chờ xác nhận");
        testRefund.setRequestedAt(new Date());
        testRefund = refundRepository.save(testRefund);
    }

    /**
     * Test 1: Lấy tất cả yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API GET /api/refund lấy danh sách tất cả refunds
     * Input: page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả refunds - Thành công")
    void testGetAllRefunds_Success() throws Exception {
        mockMvc.perform(get("/api/refund")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].refundStatus").value("Chờ xác nhận"));
    }

    /**
     * Test 2: Lấy chi tiết yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API GET /api/refund/{id} lấy thông tin chi tiết refund
     * Input: refundId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 2: Lấy refund theo ID - Thành công")
    void testGetRefundById_Success() throws Exception {
        mockMvc.perform(get("/api/refund/{id}", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(100000))
                .andExpect(jsonPath("$.refundStatus").value("Chờ xác nhận"));
    }

    /**
     * Test 3: Lấy refund với ID không tồn tại
     * Mục đích: Kiểm tra API GET /api/refund/{id} trả lỗi 404 khi refundId không hợp lệ
     * Input: refundId không tồn tại
     */
    @Test
    @DisplayName("Integration Test 3: Lấy refund theo ID - Không tồn tại")
    void testGetRefundById_NotFound() throws Exception {
        mockMvc.perform(get("/api/refund/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Tạo yêu cầu hoàn tiền mới
     * Mục đích: Kiểm tra API POST /api/refund tạo refund mới vào database
     * Input: CreateRefundRequest (orderId, orderItemId, reason, refundAmount, imageUrls)
     */
    @Test
    @DisplayName("Integration Test 4: Tạo refund mới - Thành công")
    void testCreateRefund_Success() throws Exception {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId(testOrder.getOrderId());
        request.setOrderItemId(testOrderItem.getOrderDetailId());
        request.setReason("Hàng bị hư hỏng");
        request.setRefundAmount(50000L);
        request.setImageUrls(Arrays.asList("image1.jpg", "image2.jpg"));

        mockMvc.perform(post("/api/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refundAmount").value(50000))
                .andExpect(jsonPath("$.data.refundStatus").value("Chờ xác nhận"));

        // Verify trong database
        long count = refundRepository.count();
        assert count == 2;
    }

    /**
     * Test 5: Lọc refunds theo trạng thái
     * Mục đích: Kiểm tra API GET /api/refund/status/{status} lấy refunds theo status
     * Input: status="Chờ xác nhận", page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 5: Lấy refunds theo status - Thành công")
    void testGetRefundsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/refund/status/{status}", "Chờ xác nhận")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].refundStatus").value("Chờ xác nhận"));
    }

    /**
     * Test 6: Lấy refunds của một đơn hàng
     * Mục đích: Kiểm tra API GET /api/refund/order/{orderId} lấy tất cả refunds của một order
     * Input: orderId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 6: Lấy refunds theo orderId - Thành công")
    void testGetRefundsByOrderId_Success() throws Exception {
        mockMvc.perform(get("/api/refund/order/{orderId}", testOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].order.orderId").value(testOrder.getOrderId()));
    }

    /**
     * Test 7: Cập nhật trạng thái hoàn tiền
     * Mục đích: Kiểm tra API PUT /api/refund/{id}/status cập nhật refundStatus
     * Input: UpdateRefundStatusRequest (refundStatus="Đã duyệt")
     */
    @Test
    @DisplayName("Integration Test 7: Cập nhật refund status - Thành công")
    void testUpdateRefundStatus_Success() throws Exception {
        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setRefundStatus("Đã duyệt");

        mockMvc.perform(put("/api/refund/{id}/status", testRefund.getRefundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("Đã duyệt"));

        // Verify trong database
        Refund updated = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert updated.getRefundStatus().equals("Đã duyệt");
    }

    /**
     * Test 8: Duyệt yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API PUT /api/refund/{id}/approve đổi status thành "Đã duyệt"
     * Input: refundId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 8: Approve refund - Thành công")
    void testApproveRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refund/{id}/approve", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("Đã duyệt"));

        // Verify trong database
        Refund approved = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert approved.getRefundStatus().equals("Đã duyệt");
    }

    /**
     * Test 9: Từ chối yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API PUT /api/refund/{id}/reject đổi status thành "Từ chối"
     * Input: refundId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 9: Reject refund - Thành công")
    void testRejectRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refund/{id}/reject", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("Từ chối"));

        // Verify trong database
        Refund rejected = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert rejected.getRefundStatus().equals("Từ chối");
    }

    /**
     * Test 10: Hoàn tất hoàn tiền
     * Mục đích: Kiểm tra API PUT /api/refund/{id}/complete đổi status thành "Hoàn thành"
     * Input: refundId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 10: Complete refund - Thành công")
    void testCompleteRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refund/{id}/complete", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("Hoàn thành"));

        // Verify trong database
        Refund completed = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert completed.getRefundStatus().equals("Hoàn thành");
    }

    /**
     * Test 11: Hủy/Xóa yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API DELETE /api/refund/{id} xóa refund khỏi database
     * Input: refundId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 11: Cancel/Delete refund - Thành công")
    void testCancelRefund_Success() throws Exception {
        mockMvc.perform(delete("/api/refund/{id}", testRefund.getRefundId()))
                .andExpect(status().isOk());

        // Verify trong database
        boolean exists = refundRepository.existsById(testRefund.getRefundId());
        assert !exists;
    }

    /**
     * Test 12: Tìm kiếm yêu cầu hoàn tiền
     * Mục đích: Kiểm tra API GET /api/refund/search tìm refunds theo từ khóa
     * Input: keyword="Sản phẩm", page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 12: Search refunds - Thành công")
    void testSearchRefunds_Success() throws Exception {
        mockMvc.perform(get("/api/refund/search")
                        .param("keyword", "Sản phẩm")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test 13: Đếm số yêu cầu chờ xác nhận
     * Mục đích: Kiểm tra API GET /api/refund/stats/pending-count đếm refunds "Chờ xác nhận"
     * Input: Không có
     */
    @Test
    @DisplayName("Integration Test 13: Get pending refunds count - Thành công")
    void testGetPendingRefundsCount_Success() throws Exception {
        mockMvc.perform(get("/api/refund/stats/pending-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    /**
     * Test 14: Tạo refund với đơn hàng không tồn tại
     * Mục đích: Kiểm tra API POST /api/refund trả lỗi 4xx khi orderId không hợp lệ
     * Input: orderId không tồn tại
     */
    @Test
    @DisplayName("Integration Test 14: Tạo refund - Order không tồn tại")
    void testCreateRefund_OrderNotFound() throws Exception {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setOrderId("invalid-order-id");
        request.setReason("Test");
        request.setRefundAmount(100000L);

        mockMvc.perform(post("/api/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
