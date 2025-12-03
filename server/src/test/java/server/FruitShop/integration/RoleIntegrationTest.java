package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Role.CreateRoleRequest;
import server.FruitShop.dto.request.Role.UpdateRoleRequest;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.RoleRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Role API
 * Roles: CUSTOMER, ADMIN, PREMIUM_CUSTOMER, v.v.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class RoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo role test với tên unique để tránh conflict
        testRole = new Role();
        testRole.setRoleName("TEST_CUSTOMER_" + System.currentTimeMillis());
        testRole = roleRepository.save(testRole);
    }

    /**
     * Test 1: Lấy tất cả vai trò
     * Mục đích: Kiểm tra API GET /api/role lấy danh sách tất cả roles
     * Input: Không có
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
     * Input: roleId hợp lệ
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
     * Input: roleId không tồn tại
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
     * Input: CreateRoleRequest (roleName="ADMIN")
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
     * Input: UpdateRoleRequest (roleName="PREMIUM_CUSTOMER")
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
     * Mức đích: Kiểm tra API DELETE /api/role/{id} xóa role khỏi database
     * Input: roleId hợp lệ
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
     * Input: roleName của testRole
     */
    @Test
    @DisplayName("Integration Test 7: Lấy role theo tên - Thành công")
    void testGetRoleByName_Success() throws Exception {
        mockMvc.perform(get("/api/role/name/{name}", testRole.getRoleName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value(testRole.getRoleName()));
    }

    /**
     * Test 8: Tìm kiếm vai trò
     * Mục đích: Kiểm tra API GET /api/role/search/{name} tìm roles có tên chứa keyword
     * Input: keyword từ testRole name
     */
    @Test
    @DisplayName("Integration Test 8: Tìm kiếm role theo tên - Thành công")
    void testSearchRolesByName_Success() throws Exception {
        // Tạo thêm role khác
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN_" + System.currentTimeMillis());
        roleRepository.save(adminRole);

        // Tìm kiếm role với từ khóa từ testRole
        String searchKeyword = testRole.getRoleName().substring(0, 8); // Lấy 8 ký tự đầu
        mockMvc.perform(get("/api/role/search/{name}", searchKeyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
