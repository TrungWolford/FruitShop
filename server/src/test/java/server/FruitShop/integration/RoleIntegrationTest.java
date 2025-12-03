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
        // Xóa dữ liệu cũ
        roleRepository.deleteAll();

        // Tạo role test
        testRole = new Role();
        testRole.setRoleName("CUSTOMER");
        testRole = roleRepository.save(testRole);
    }

    @Test
    @DisplayName("Integration Test 1: Lấy tất cả roles - Thành công")
    void testGetAllRoles_Success() throws Exception {
        mockMvc.perform(get("/api/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].roleName").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Integration Test 2: Lấy role theo ID - Thành công")
    void testGetRoleById_Success() throws Exception {
        mockMvc.perform(get("/api/role/{id}", testRole.getRoleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Integration Test 3: Lấy role theo ID - Không tồn tại")
    void testGetRoleById_NotFound() throws Exception {
        mockMvc.perform(get("/api/role/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

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
        assert count == 2;
    }

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

    @Test
    @DisplayName("Integration Test 6: Xóa role - Thành công")
    void testDeleteRole_Success() throws Exception {
        mockMvc.perform(delete("/api/role/{id}", testRole.getRoleId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = roleRepository.existsById(testRole.getRoleId());
        assert !exists;
    }

    @Test
    @DisplayName("Integration Test 7: Lấy role theo tên - Thành công")
    void testGetRoleByName_Success() throws Exception {
        mockMvc.perform(get("/api/role/name/{name}", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Integration Test 8: Tìm kiếm role theo tên - Thành công")
    void testSearchRolesByName_Success() throws Exception {
        // Tạo thêm role
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");
        roleRepository.save(adminRole);

        mockMvc.perform(get("/api/role/search/{name}", "CUST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roleName", containsString("CUSTOMER")));
    }
}
