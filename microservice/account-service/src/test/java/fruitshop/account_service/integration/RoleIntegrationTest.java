package fruitshop.account_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.account_service.dto.request.Role.CreateRoleRequest;
import fruitshop.account_service.dto.request.Role.UpdateRoleRequest;
import fruitshop.account_service.entity.Role;
import fruitshop.account_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Role API
 * Roles: CUSTOMER, ADMIN, PREMIUM_CUSTOMER, v.v.
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class RoleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        
        // Tạo role test với tên unique để tránh conflict
        testRole = new Role();
        testRole.setRoleName("TEST_CUSTOMER_" + System.currentTimeMillis());
        testRole = roleRepository.save(testRole);
    }

    /**
     * Test 1: Lấy tất cả vai trò
     * Mục đích: Kiểm tra API GET /api/role lấy danh sách tất cả roles
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả roles - Thành công")
    void testGetAllRoles_Success() throws Exception {
        mockMvc.perform(get("/api/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test 2: Lấy chi tiết vai trò
     * Mục đích: Kiểm tra API GET /api/role/{id} lấy thông tin role theo ID
     */
    @Test
    @DisplayName("Integration Test 2: Lấy role theo ID - Thành công")
    void testGetRoleById_Success() throws Exception {
        mockMvc.perform(get("/api/role/{id}", testRole.getRoleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value(testRole.getRoleName()));
    }

    /**
     * Test 3: Lấy role với ID không tồn tại
     * Mục đích: Kiểm tra API GET /api/role/{id} trả lỗi 404 khi roleId không hợp lệ
     */
    @Test
    @DisplayName("Integration Test 3: Lấy role theo ID - Không tồn tại")
    void testGetRoleById_NotFound() throws Exception {
        mockMvc.perform(get("/api/role/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Tạo vai trò mới
     * Mục đích: Kiểm tra API POST /api/role tạo role mới vào database
     */
    @Test
    @DisplayName("Integration Test 4: Tạo role mới - Thành công")
    void testCreateRole_Success() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("ADMIN");

        mockMvc.perform(post("/api/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));

        // Verify trong database
        long count = roleRepository.count();
        assert count >= 2; // Ít nhất 2 roles (testRole + ADMIN)
    }

    /**
     * Test 5: Cập nhật tên vai trò
     * Mục đích: Kiểm tra API PUT /api/role/{id} cập nhật roleName
     */
    @Test
    @DisplayName("Integration Test 5: Cập nhật role - Thành công")
    void testUpdateRole_Success() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("PREMIUM_CUSTOMER");

        mockMvc.perform(put("/api/role/{id}", testRole.getRoleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("PREMIUM_CUSTOMER"));

        // Verify trong database
        Role updated = roleRepository.findById(testRole.getRoleId()).orElseThrow();
        assert updated.getRoleName().equals("PREMIUM_CUSTOMER");
    }

    /**
     * Test 6: Xóa vai trò
     * Mục đích: Kiểm tra API DELETE /api/role/{id} xóa role khỏi database
     */
    @Test
    @DisplayName("Integration Test 6: Xóa role - Thành công")
    void testDeleteRole_Success() throws Exception {
        mockMvc.perform(delete("/api/role/{id}", testRole.getRoleId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = roleRepository.existsById(testRole.getRoleId());
        assert !exists;
    }

    /**
     * Test 7: Lấy vai trò theo tên
     * Mục đích: Kiểm tra API GET /api/role/name/{name} lấy role theo roleName
     */
    @Test
    @DisplayName("Integration Test 7: Lấy role theo tên - Thành công")
    void testGetRoleByName_Success() throws Exception {
        mockMvc.perform(get("/api/role/name/{name}", testRole.getRoleName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value(testRole.getRoleName()));
    }
}
