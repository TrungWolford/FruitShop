package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.FruitShop.dto.request.Role.CreateRoleRequest;
import server.FruitShop.dto.request.Role.UpdateRoleRequest;
import server.FruitShop.dto.response.Role.RoleResponse;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.RoleRepository;
import server.FruitShop.service.Impl.RoleServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho RoleService
 * Class này test các chức năng quản lý vai trò (role) trong hệ thống
 * bao gồm: CRUD operations, tìm kiếm role theo tên, và các validation
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Role Service")
class RoleServiceImplTest {

    @Mock // Mock repository quản lý vai trò
    private RoleRepository roleRepository;

    @InjectMocks // Inject mock vào service để test
    private RoleServiceImpl roleService;

    // Entity mẫu để test
    private Role testRole;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo role mẫu với ID và tên vai trò CUSTOMER
     */
    @BeforeEach
    void setUp() {
        // Tạo role test với vai trò CUSTOMER
        testRole = new Role();
        testRole.setRoleId("role-001");
        testRole.setRoleName("CUSTOMER"); // Vai trò khách hàng
    }

    /**
     * Test 1: Lấy danh sách tất cả roles - Trường hợp thành công
     * Kịch bản: Admin xem toàn bộ vai trò có trong hệ thống
     * Kỳ vọng: Trả về list chứa tất cả roles (CUSTOMER, ADMIN, v.v.)
     */
    @Test
    @DisplayName("Test 1: Lấy tất cả roles - Thành công")
    void testGetAllRoles_Success() {
        // ARRANGE: Giả lập repository trả về danh sách roles
        List<Role> roles = List.of(testRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // ACT: Gọi service lấy tất cả roles
        List<RoleResponse> result = roleService.getAllRoles();

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // List không null
        assertEquals(1, result.size()); // Có 1 role
        assertEquals("CUSTOMER", result.get(0).getRoleName()); // Role là CUSTOMER
        verify(roleRepository, times(1)).findAll(); // Gọi repo 1 lần
    }

    /**
     * Test 2: Lấy thông tin role theo ID - Trường hợp thành công
     * Kịch bản: Tìm role với ID hợp lệ đang tồn tại
     * Kỳ vọng: Trả về RoleResponse với đầy đủ thông tin role
     */
    @Test
    @DisplayName("Test 2: Lấy role theo ID - Thành công")
    void testGetRoleById_Success() {
        // ARRANGE: Giả lập tìm thấy role
        when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));

        // ACT: Gọi service lấy role theo ID
        RoleResponse result = roleService.getRoleById("role-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        assertEquals("role-001", result.getRoleId()); // ID đúng
        assertEquals("CUSTOMER", result.getRoleName()); // Tên role đúng
        verify(roleRepository, times(1)).findById("role-001"); // Gọi repo 1 lần
    }

    /**
     * Test 3: Lấy role theo ID - Trường hợp không tìm thấy
     * Kịch bản: Tìm role với ID không tồn tại trong hệ thống
     * Kỳ vọng:Ném RuntimeException với message "Role not found"
     */
    @Test
    @DisplayName("Test 3: Lấy role theo ID - Không tìm thấy")
    void testGetRoleById_NotFound() {
        // ARRANGE: Giả lập không tìm thấy role
        when(roleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.getRoleById("invalid-id"); // Phải ném exception
        });

        assertTrue(exception.getMessage().contains("Role not found")); // Message đúng
        verify(roleRepository, times(1)).findById("invalid-id"); // Đã gọi repo
    }

    /**
     * Test 4: Tạo role mới - Trường hợp thành công
     * Kịch bản: Admin tạo vai trò mới trong hệ thống (ví dụ: ADMIN, STAFF)
     * Kỳ vọng: Role được tạo và lưu vào DB thành công
     */
    @Test
    @DisplayName("Test 4: Tạo role mới - Thành công")
    void testCreateRole_Success() {
        // ARRANGE: Chuẩn bị request tạo role
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("ADMIN"); // Tạo vai trò ADMIN

        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // ACT: Gọi service tạo role
        RoleResponse result = roleService.createRole(request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(roleRepository, times(1)).save(any(Role.class)); // Đã lưu vào DB
    }

    /**
     * Test 5: Cập nhật thông tin role - Trường hợp thành công
     * Kịch bản: Admin sửa tên role (ví dụ: CUSTOMER → VIP_CUSTOMER)
     * Kỳ vọng: Role được cập nhật và lưu lại trong DB
     */
    @Test
    @DisplayName("Test 5: Cập nhật role - Thành công")
    void testUpdateRole_Success() {
        // ARRANGE: Chuẩn bị request cập nhật
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("VIP_CUSTOMER"); // Đổi thành VIP_CUSTOMER

        when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // ACT: Gọi service cập nhật role
        RoleResponse result = roleService.updateRole("role-001", request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(roleRepository, times(1)).findById("role-001"); // Đã tìm role
        verify(roleRepository, times(1)).save(any(Role.class)); // Đã lưu cập nhật
    }

    /**
     * Test 6: Cập nhật role - Trường hợp không tìm thấy
     * Kịch bản: Cập nhật role với ID không tồn tại
     * Kỳ vọng: Nem RuntimeException, không lưu gì vào DB
     */
    @Test
    @DisplayName("Test 6: Cập nhật role - Không tìm thấy")
    void testUpdateRole_NotFound() {
        // ARRANGE: Chuẩn bị request với ID không hợp lệ
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("TEST");

        when(roleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.updateRole("invalid-id", request); // Phải ném exception
        });

        assertTrue(exception.getMessage().contains("Role not found")); // Message đúng
        verify(roleRepository, times(1)).findById("invalid-id"); // Đã tìm role
        verify(roleRepository, never()).save(any(Role.class)); // Không lưu
    }

    /**
     * Test 7: Xóa role - Trường hợp thành công
     * Kịch bản: Admin xóa vai trò không còn sử dụng
     * Kỳ vọng: Role bị xóa khỏi hệ thống
     */
    @Test
    @DisplayName("Test 7: Xóa role - Thành công")
    void testDeleteRole_Success() {
        // ARRANGE: Giả lập role tồn tại
        when(roleRepository.existsById("role-001")).thenReturn(true);
        doNothing().when(roleRepository).deleteById("role-001");

        // ACT: Gọi service xóa role
        roleService.deleteRole("role-001");

        // ASSERT: Kiểm tra đã xóa
        verify(roleRepository, times(1)).existsById("role-001"); // Đã kiểm tra tồn tại
        verify(roleRepository, times(1)).deleteById("role-001"); // Đã xóa
    }

    /**
     * Test 8: Xóa role - Trường hợp không tìm thấy
     * Kịch bản: Xóa role với ID không tồn tại
     * Kỳ vọng: Nem RuntimeException, không thực hiện xóa
     */
    @Test
    @DisplayName("Test 8: Xóa role - Không tìm thấy")
    void testDeleteRole_NotFound() {
        // ARRANGE: Giả lập role không tồn tại
        when(roleRepository.existsById("invalid-id")).thenReturn(false);

        // ACT & ASSERT: Gọi service và kiểm tra exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.deleteRole("invalid-id"); // Phải ném exception
        });

        assertTrue(exception.getMessage().contains("Role not found")); // Message đúng
        verify(roleRepository, times(1)).existsById("invalid-id"); // Đã kiểm tra
        verify(roleRepository, never()).deleteById(any()); // Không xóa
    }

    /**
     * Test 9: Lấy role theo tên - Trường hợp thành công
     * Kịch bản: Tìm role bằng tên chính xác (CUSTOMER, ADMIN, v.v.)
     * Kỳ vọng: Trả về RoleResponse chứa thông tin role tương ứng
     */
    @Test
    @DisplayName("Test 9: Lấy role theo tên - Thành công")
    void testGetRoleByName_Success() {
        // ARRANGE: Giả lập tìm thấy role theo tên
        when(roleRepository.findByRoleName("CUSTOMER")).thenReturn(Optional.of(testRole));

        // ACT: Gọi service tìm role theo tên
        RoleResponse result = roleService.getRoleByName("CUSTOMER");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        assertEquals("CUSTOMER", result.getRoleName()); // Tên role đúng
        verify(roleRepository, times(1)).findByRoleName("CUSTOMER"); // Gọi repo 1 lần
    }

    /**
     * Test 10: Lấy role theo tên - Trường hợp không tìm thấy
     * Kịch bản: Tìm role với tên không tồn tại trong hệ thống
     * Kỳ vọng: Nem RuntimeException với message "Role not found"
     */
    @Test
    @DisplayName("Test 10: Lấy role theo tên - Không tìm thấy")
    void testGetRoleByName_NotFound() {
        // ARRANGE: Giả lập không tìm thấy role
        when(roleRepository.findByRoleName("NOTEXIST")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.getRoleByName("NOTEXIST"); // Phải ném exception
        });

        assertTrue(exception.getMessage().contains("Role not found")); // Message đúng
        verify(roleRepository, times(1)).findByRoleName("NOTEXIST"); // Đã gọi repo
    }

    /**
     * Test 11: Tìm kiếm roles theo tên (partial match) - Trường hợp có kết quả
     * Kịch bản: Admin tìm kiếm roles bằng từ khóa (ví dụ: "CUST" → tìm thấy "CUSTOMER")
     * Kỳ vọng: Trả về list các roles có tên chứa từ khóa tìm kiếm
     */
    @Test
    @DisplayName("Test 11: Tìm kiếm roles theo tên - Thành công")
    void testSearchRolesByName_Success() {
        // ARRANGE: Giả lập tìm thấy roles chứa từ khóa
        List<Role> roles = List.of(testRole);
        when(roleRepository.findByRoleNameContaining("CUST")).thenReturn(roles);

        // ACT: Gọi service tìm kiếm roles
        List<RoleResponse> result = roleService.searchRolesByName("CUST");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // List không null
        assertEquals(1, result.size()); // Tìm thấy 1 role
        assertEquals("CUSTOMER", result.get(0).getRoleName()); // Tên role chứa "CUST"
        verify(roleRepository, times(1)).findByRoleNameContaining("CUST"); // Gọi repo 1 lần
    }

    /**
     * Test 12: Tìm kiếm roles - Trường hợp không có kết quả
     * Kịch bản: Tìm kiếm với từ khóa không khớp với bất kỳ role nào
     * Kỳ vọng: Trả về list rỗng (không có kết quả)
     */
    @Test
    @DisplayName("Test 12: Tìm kiếm roles - Không có kết quả")
    void testSearchRolesByName_NoResults() {
        // ARRANGE: Giả lập không tìm thấy role nào
        when(roleRepository.findByRoleNameContaining("NOTEXIST")).thenReturn(List.of());

        // ACT: Gọi service tìm kiếm
        List<RoleResponse> result = roleService.searchRolesByName("NOTEXIST");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // List không null
        assertTrue(result.isEmpty()); // List rỗng
        verify(roleRepository, times(1)).findByRoleNameContaining("NOTEXIST"); // Đã gọi repo
    }
}
