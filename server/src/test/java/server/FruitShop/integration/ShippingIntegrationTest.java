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
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Shipping API
 * Shipping Status: 0=Vô hiệu hóa, 1=Hoạt động
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class ShippingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Shipping testShipping;
    private Account testAccount;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // Không cần deleteAll() vì @Transactional sẽ tự rollback sau mỗi test
        
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
    }

    /**
     * Test 1: Lấy tất cả địa chỉ giao hàng
     * Mục đích: Kiểm tra API GET /api/shipping lấy danh sách tất cả shippings
     * Input: page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả shippings - Thành công")
    void testGetAllShippings_Success() throws Exception {
        mockMvc.perform(get("/api/shipping")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test 2: Lấy chi tiết địa chỉ giao hàng
     * Mục đích: Kiểm tra API GET /api/shipping/{id} lấy thông tin shipping theo ID
     * Input: shippingId hợp lệ
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
     * Input: shippingId không tồn tại
     */
    @Test
    @DisplayName("Integration Test 3: Lấy shipping theo ID - Không tồn tại")
    void testGetShippingById_NotFound() throws Exception {
        mockMvc.perform(get("/api/shipping/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Lấy địa chỉ giao hàng của một tài khoản
     * Mục đích: Kiểm tra API GET /api/shipping/account/{accountId} lấy tất cả shippings của account
     * Input: accountId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 4: Lấy shippings theo accountId - Thành công")
    void testGetShippingsByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/account/{accountId}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].accountId").value(testAccount.getAccountId()));
    }

    /**
     * Test 5: Tạo địa chỉ giao hàng mới
     * Mục đích: Kiểm tra API POST /api/shipping tạo shipping mới vào database
     * Input: ShippingRequest (accountId, receiverName, receiverPhone, receiverAddress, city, shippingFee, status)
     */
    @Test
    @DisplayName("Integration Test 5: Tạo shipping mới - Thành công")
    void testCreateShipping_Success() throws Exception {
        ShippingRequest request = new ShippingRequest();
        request.setAccountId(testAccount.getAccountId());
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
        assert count == 2;
    }

    /**
     * Test 6: Cập nhật địa chỉ giao hàng
     * Mục đích: Kiểm tra API PUT /api/shipping/{id} cập nhật thông tin shipping
     * Input: ShippingRequest (receiverName, receiverAddress, shippingFee updated)
     */
    @Test
    @DisplayName("Integration Test 6: Cập nhật shipping - Thành công")
    void testUpdateShipping_Success() throws Exception {
        ShippingRequest request = new ShippingRequest();
        request.setAccountId(testAccount.getAccountId());
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
     * Input: shippingId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 7: Xóa shipping - Thành công")
    void testDeleteShipping_Success() throws Exception {
        mockMvc.perform(delete("/api/shipping/{id}", testShipping.getShippingId()))
                .andExpect(status().isOk());

        // Verify trong database
        boolean exists = shippingRepository.existsById(testShipping.getShippingId());
        assert !exists;
    }

    /**
     * Test 8: Vô hiệu hóa/Kích hoạt địa chỉ
     * Mục đích: Kiểm tra API PUT /api/shipping/{id}/status cập nhật trạng thái shipping
     * Input: status=0 (vô hiệu hóa)
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

    /**
     * Test 9: Lọc địa chỉ theo trạng thái
     * Mục đích: Kiểm tra API GET /api/shipping/filter lọc shippings theo status
     * Input: status=1, page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 9: Filter shippings theo status - Thành công")
    void testFilterShippingsByStatus_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/filter")
                        .param("status", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test 10: Tìm kiếm địa chỉ giao hàng
     * Mục đích: Kiểm tra API GET /api/shipping/search tìm shippings theo keyword
     * Input: keyword="Nguyễn", page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 10: Search shippings - Thành công")
    void testSearchShippings_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/search")
                        .param("keyword", "Nguyễn")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test 11: Tìm kiếm và lọc địa chỉ
     * Mục đích: Kiểm tra API GET /api/shipping/search-filter kết hợp tìm kiếm và lọc
     * Input: keyword="Nguyễn", status=1, page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 11: Search và filter shippings - Thành công")
    void testSearchAndFilterShippings_Success() throws Exception {
        mockMvc.perform(get("/api/shipping/search-filter")
                        .param("keyword", "Nguyễn")
                        .param("status", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
