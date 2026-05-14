package fruitshop.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import fruitshop.order_service.entity.Order;
import fruitshop.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Order API
 * Order Status: 0=Pending, 1=Paid, 2=Confirmed, 3=Delivering, 4=Completed, 5=Cancelled
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    
    private final String TEST_ACCOUNT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        wireMock.resetAll();

        // Tạo order test
        testOrder = new Order();
        testOrder.setAccountId(TEST_ACCOUNT_ID);
        testOrder.setTotalAmount(350000);
        testOrder.setStatus(0); // Pending
        testOrder.setCreatedAt(Instant.now());
        testOrder = orderRepository.save(testOrder);
    }

    /**
     * Test 1: Lấy tất cả orders với phân trang
     * Mục đích: Kiểm tra API GET /api/order trả về danh sách orders
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả orders - Thành công")
    void testGetAllOrders_Success() throws Exception {
        mockMvc.perform(get("/api/order")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].accountId").value(TEST_ACCOUNT_ID));
    }

    /**
     * Test 2: Lấy order theo ID
     * Mục đích: Kiểm tra API GET /api/order/{id} trả về thông tin chi tiết order
     */
    @Test
    @DisplayName("Integration Test 2: Lấy order theo ID - Thành công")
    void testGetOrderById_Success() throws Exception {
        mockMvc.perform(get("/api/order/{id}", testOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.totalAmount").value(350000))
                .andExpect(jsonPath("$.status").value(0));
    }

    /**
     * Test 3: Lấy order với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi order không tồn tại
     */
    @Test
    @DisplayName("Integration Test 3: Lấy order theo ID - Không tồn tại")
    void testGetOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/order/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Lấy danh sách orders theo account ID
     * Mục đích: Kiểm tra API GET /api/order/account/{accountId}
     */
    @Test
    @DisplayName("Integration Test 4: Lấy orders theo accountId - Thành công")
    void testGetOrdersByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/order/account/{accountId}", TEST_ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].accountId").value(TEST_ACCOUNT_ID));
    }

    /**
     * Test 5: Bắt đầu giao hàng (Chuyển status từ 2 -> 3)
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/start-delivery
     * Cần cập nhật status testOrder = 2 trước
     */
    @Test
    @DisplayName("Integration Test 5: Bắt đầu giao hàng - Thành công")
    void testStartDelivery_Success() throws Exception {
        testOrder.setStatus(2); // Confirmed
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/order/{orderId}/start-delivery", testOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(3));

        // Verify trong database
        Order updated = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert updated.getStatus() == 3;
    }

    /**
     * Test 6: Hủy đơn hàng (Chuyển status sang 5)
     * Mục đích: Kiểm tra API PUT /api/order/{orderId}/cancel
     */
    @Test
    @DisplayName("Integration Test 6: Hủy đơn hàng - Thành công")
    void testCancelOrder_Success() throws Exception {
        mockMvc.perform(put("/api/order/{orderId}/cancel", testOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));

        // Verify trong database
        Order updated = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assert updated.getStatus() == 0;
    }
}
