package fruitshop.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.entity.Refund;
import fruitshop.order_service.repository.OrderItemRepository;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Refund API trong order-service
 * Kiểm tra toàn bộ luồng xử lý: Controller → Service → Repository → Database
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class RefundIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    private OrderItem testOrderItem;
    private Refund testRefund;

    private final String TEST_ACCOUNT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        refundRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();

        // 1. Tạo Order
        testOrder = new Order();
        testOrder.setAccountId(TEST_ACCOUNT_ID);
        testOrder.setTotalAmount(500000);
        testOrder.setStatus(1); // Paid
        testOrder.setCreatedAt(Instant.now());
        testOrder = orderRepository.save(testOrder);

        // 2. Tạo OrderItem
        testOrderItem = new OrderItem();
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProductId("PROD-123");
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(250000);
        testOrderItem.setStatus("DELIVERED");
        testOrderItem = orderItemRepository.save(testOrderItem);

        // 3. Tạo Refund
        testRefund = new Refund();
        testRefund.setOrder(testOrder);
        testRefund.setOrderItem(testOrderItem);
        testRefund.setReason("Hàng lỗi kỹ thuật");
        testRefund.setRefundAmount(250000);
        testRefund.setRefundStatus("PENDING");
        testRefund.setRequestedAt(Instant.now());
        testRefund = refundRepository.save(testRefund);
    }

    @Test
    @DisplayName("Test 1: Lấy danh sách tất cả refunds có phân trang - Thành công")
    void testGetAllRefunds_Success() throws Exception {
        mockMvc.perform(get("/api/refunds")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].reason").value("Hàng lỗi kỹ thuật"))
                .andExpect(jsonPath("$.content[0].refundStatus").value("PENDING"));
    }

    @Test
    @DisplayName("Test 2: Lấy chi tiết refund theo ID - Thành công")
    void testGetRefundById_Success() throws Exception {
        mockMvc.perform(get("/api/refunds/{refundId}", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(250000))
                .andExpect(jsonPath("$.refundStatus").value("PENDING"));
    }

    @Test
    @DisplayName("Test 3: Lấy chi tiết refund theo ID - Không tồn tại (trả về 400 do ném IllegalArgumentException)")
    void testGetRefundById_NotFound() throws Exception {
        mockMvc.perform(get("/api/refunds/{refundId}", "invalid-refund-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test 4: Tạo yêu cầu hoàn tiền trực tiếp qua POST /api/refunds - Thành công")
    void testCreateRefundDirect_Success() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", testOrder.getOrderId());
        request.put("orderItemId", testOrderItem.getOrderItemId());
        request.put("reason", "Sản phẩm không đúng mô tả");
        request.put("refundAmount", 100000);

        mockMvc.perform(post("/api/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(100000))
                .andExpect(jsonPath("$.refundStatus").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("Sản phẩm không đúng mô tả"));

        assert refundRepository.count() == 2;
    }

    @Test
    @DisplayName("Test 5: Lấy danh sách refunds theo trạng thái - Thành công")
    void testGetRefundsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/refunds/status/{status}", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].refundStatus").value("PENDING"));
    }

    @Test
    @DisplayName("Test 6: Lấy danh sách hoàn tiền theo Order ID - Thành công")
    void testGetRefundsByOrderId_Success() throws Exception {
        mockMvc.perform(get("/api/orders/{orderId}/refunds", testOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].reason").value("Hàng lỗi kỹ thuật"));
    }

    @Test
    @DisplayName("Test 7: Duyệt hoàn tiền qua endpoint approve - Thành công")
    void testApproveRefund_Success() throws Exception {
        mockMvc.perform(put("/api/orders/{orderId}/refunds/{refundId}/approve", testOrder.getOrderId(), testRefund.getRefundId())
                        .param("approverName", "Admin Approver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("APPROVED"));

        Refund approved = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert approved.getRefundStatus().equals("APPROVED");
    }

    @Test
    @DisplayName("Test 8: Từ chối yêu cầu hoàn tiền (reject) - Thành công")
    void testRejectRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refunds/{refundId}/reject", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("REJECTED"));

        Refund rejected = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert rejected.getRefundStatus().equals("REJECTED");
        assert rejected.getProcessedAt() != null;
    }

    @Test
    @DisplayName("Test 9: Hoàn tất quá trình hoàn tiền (complete) - Thành công")
    void testCompleteRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refunds/{refundId}/complete", testRefund.getRefundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundStatus").value("COMPLETED"));

        Refund completed = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert completed.getRefundStatus().equals("COMPLETED");
        assert completed.getProcessedAt() != null;
    }

    @Test
    @DisplayName("Test 10: Hủy yêu cầu hoàn tiền qua endpoint cancel - Thành công")
    void testCancelRefund_Success() throws Exception {
        mockMvc.perform(put("/api/refunds/{refundId}/cancel", testRefund.getRefundId()))
                .andExpect(status().isOk());

        Refund cancelled = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert cancelled.getRefundStatus().equals("CANCELLED");
    }

    @Test
    @DisplayName("Test 11: Xóa yêu cầu hoàn tiền (soft delete chuyển sang CANCELLED) - Thành công")
    void testDeleteRefund_Success() throws Exception {
        mockMvc.perform(delete("/api/refunds/{refundId}", testRefund.getRefundId()))
                .andExpect(status().isNoContent());

        Refund deleted = refundRepository.findById(testRefund.getRefundId()).orElseThrow();
        assert deleted.getRefundStatus().equals("CANCELLED");
    }
}
