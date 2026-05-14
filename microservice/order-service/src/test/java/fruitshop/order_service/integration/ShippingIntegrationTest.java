package fruitshop.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.order_service.dto.request.ShippingRequest;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.repository.ShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Shipping API
 * Shipping Status: 0=Vô hiệu hóa, 1=Hoạt động
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class ShippingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Shipping testShipping;
    
    private final String TEST_ACCOUNT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        shippingRepository.deleteAll();
        
        // Tạo shipping test
        testShipping = new Shipping();
        testShipping.setAccountId(TEST_ACCOUNT_ID);
        testShipping.setReceiverName("Nguyễn Văn A");
        testShipping.setReceiverPhone("0355142890");
        testShipping.setReceiverAddress("123 Đường ABC, Quận 1, TP.HCM");
        testShipping.setCity("TP.HCM");
        testShipping.setShipperName("");
        testShipping.setShippingFee(30000);
        testShipping.setStatus(1);
        testShipping = shippingRepository.save(testShipping);
    }

    /**
     * Test 1: Lấy tất cả địa chỉ giao hàng
     * Mục đích: Kiểm tra API GET /api/shipping lấy danh sách tất cả shippings
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả shippings - Thành công")
    void testGetAllShippings_Success() throws Exception {
        mockMvc.perform(get("/api/shipping")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test 2: Lấy chi tiết địa chỉ giao hàng
     * Mục đích: Kiểm tra API GET /api/shipping/{id} lấy thông tin shipping theo ID
     */
    @Test
    @DisplayName("Integration Test 2: Lấy shipping theo ID - Thành công")
    void testGetShippingById_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/{id}", testShipping.getShippingId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.receiverAddress").value("123 Đường ABC, Quận 1, TP.HCM"))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test 3: Lấy shipping với ID không tồn tại
     * Mục đích: Kiểm tra API GET /api/shipping/{id} trả lỗi 404 khi shippingId không hợp lệ
     */
    @Test
    @DisplayName("Integration Test 3: Lấy shipping theo ID - Không tồn tại")
    void testGetShippingById_NotFound() throws Exception {
        mockMvc.perform(get("/api/shipping/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Lấy địa chỉ giao hàng của một tài khoản
     * Mục đích: Kiểm tra API GET /api/shipping/account/{accountId}
     */
    @Test
    @DisplayName("Integration Test 4: Lấy shippings theo accountId - Thành công")
    void testGetShippingsByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/account/{accountId}", TEST_ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].accountId").value(TEST_ACCOUNT_ID));
    }

    /**
     * Test 5: Tạo địa chỉ giao hàng mới
     * Mục đích: Kiểm tra API POST /api/shipping tạo shipping mới vào database
     */
    @Test
    @DisplayName("Integration Test 5: Tạo shipping mới - Thành công")
    void testCreateShipping_Success() throws Exception {
        ShippingRequest request = new ShippingRequest();
        request.setAccountId(TEST_ACCOUNT_ID);
        request.setReceiverName("Trần Thị B");
        request.setReceiverPhone("0999999999");
        request.setReceiverAddress("456 Đường XYZ, Quận 2, TP.HCM");
        request.setCity("TP.HCM");
        request.setShipperName("");
        request.setShippingFee(30000);
        request.setStatus(1);

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("Trần Thị B"))
                .andExpect(jsonPath("$.receiverPhone").value("0999999999"));

        // Verify trong database
        long count = shippingRepository.count();
        assert count >= 2;
    }

    /**
     * Test 6: Cập nhật địa chỉ giao hàng
     * Mục đích: Kiểm tra API PUT /api/shipping/{id} cập nhật thông tin shipping
     */
    @Test
    @DisplayName("Integration Test 6: Cập nhật shipping - Thành công")
    void testUpdateShipping_Success() throws Exception {
        ShippingRequest request = new ShippingRequest();
        request.setAccountId(TEST_ACCOUNT_ID);
        request.setReceiverName("Nguyễn Văn A Updated");
        request.setReceiverPhone("0355142890");
        request.setReceiverAddress("789 Đường Updated, Quận 3, TP.HCM");
        request.setCity("TP.HCM");
        request.setShipperName("");
        request.setShippingFee(35000);
        request.setStatus(1);

        mockMvc.perform(put("/api/shipping/{id}", testShipping.getShippingId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverName").value("Nguyễn Văn A Updated"))
                .andExpect(jsonPath("$.receiverAddress").value("789 Đường Updated, Quận 3, TP.HCM"));

        // Verify trong database
        Shipping updated = shippingRepository.findById(testShipping.getShippingId()).orElseThrow();
        assert updated.getReceiverName().equals("Nguyễn Văn A Updated");
    }

    /**
     * Test 7: Xóa địa chỉ giao hàng
     * Mục đích: Kiểm tra API DELETE /api/shipping/{id} xóa shipping khỏi database
     */
    @Test
    @DisplayName("Integration Test 7: Xóa shipping - Thành công")
    void testDeleteShipping_Success() throws Exception {
        mockMvc.perform(delete("/api/shipping/{id}", testShipping.getShippingId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = shippingRepository.existsById(testShipping.getShippingId());
        assert !exists;
    }

    /**
     * Test 8: Vô hiệu hóa/Kích hoạt địa chỉ
     * Mục đích: Kiểm tra API PUT /api/shipping/{id}/status cập nhật trạng thái shipping
     */
    @Test
    @DisplayName("Integration Test 8: Cập nhật shipping status - Thành công")
    void testUpdateShippingStatus_Success() throws Exception {
        mockMvc.perform(put("/api/shipping/{id}/status", testShipping.getShippingId())
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));

        // Verify trong database
        Shipping updated = shippingRepository.findById(testShipping.getShippingId()).orElseThrow();
        assert updated.getStatus() == 0;
    }
}
