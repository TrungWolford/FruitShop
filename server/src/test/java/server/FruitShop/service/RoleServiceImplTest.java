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
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Role Service")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleId("role-001");
        testRole.setRoleName("CUSTOMER");
    }

    @Test
    @DisplayName("Test 1: Lấy tất cả roles - Thành công")
    void testGetAllRoles_Success() {
        // ARRANGE
        List<Role> roles = List.of(testRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // ACT
        List<RoleResponse> result = roleService.getAllRoles();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CUSTOMER", result.get(0).getRoleName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 2: Lấy role theo ID - Thành công")
    void testGetRoleById_Success() {
        // ARRANGE
        when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));

        // ACT
        RoleResponse result = roleService.getRoleById("role-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("role-001", result.getRoleId());
        assertEquals("CUSTOMER", result.getRoleName());
        verify(roleRepository, times(1)).findById("role-001");
    }

    @Test
    @DisplayName("Test 3: Lấy role theo ID - Không tìm thấy")
    void testGetRoleById_NotFound() {
        // ARRANGE
        when(roleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.getRoleById("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 4: Tạo role mới - Thành công")
    void testCreateRole_Success() {
        // ARRANGE
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("ADMIN");

        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // ACT
        RoleResponse result = roleService.createRole(request);

        // ASSERT
        assertNotNull(result);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("Test 5: Cập nhật role - Thành công")
    void testUpdateRole_Success() {
        // ARRANGE
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("VIP_CUSTOMER");

        when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // ACT
        RoleResponse result = roleService.updateRole("role-001", request);

        // ASSERT
        assertNotNull(result);
        verify(roleRepository, times(1)).findById("role-001");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("Test 6: Cập nhật role - Không tìm thấy")
    void testUpdateRole_NotFound() {
        // ARRANGE
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("TEST");

        when(roleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.updateRole("invalid-id", request);
        });

        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findById("invalid-id");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Test 7: Xóa role - Thành công")
    void testDeleteRole_Success() {
        // ARRANGE
        when(roleRepository.existsById("role-001")).thenReturn(true);
        doNothing().when(roleRepository).deleteById("role-001");

        // ACT
        roleService.deleteRole("role-001");

        // ASSERT
        verify(roleRepository, times(1)).existsById("role-001");
        verify(roleRepository, times(1)).deleteById("role-001");
    }

    @Test
    @DisplayName("Test 8: Xóa role - Không tìm thấy")
    void testDeleteRole_NotFound() {
        // ARRANGE
        when(roleRepository.existsById("invalid-id")).thenReturn(false);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.deleteRole("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).existsById("invalid-id");
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Test 9: Lấy role theo tên - Thành công")
    void testGetRoleByName_Success() {
        // ARRANGE
        when(roleRepository.findByRoleName("CUSTOMER")).thenReturn(Optional.of(testRole));

        // ACT
        RoleResponse result = roleService.getRoleByName("CUSTOMER");

        // ASSERT
        assertNotNull(result);
        assertEquals("CUSTOMER", result.getRoleName());
        verify(roleRepository, times(1)).findByRoleName("CUSTOMER");
    }

    @Test
    @DisplayName("Test 10: Lấy role theo tên - Không tìm thấy")
    void testGetRoleByName_NotFound() {
        // ARRANGE
        when(roleRepository.findByRoleName("NOTEXIST")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.getRoleByName("NOTEXIST");
        });

        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findByRoleName("NOTEXIST");
    }

    @Test
    @DisplayName("Test 11: Tìm kiếm roles theo tên - Thành công")
    void testSearchRolesByName_Success() {
        // ARRANGE
        List<Role> roles = List.of(testRole);
        when(roleRepository.findByRoleNameContaining("CUST")).thenReturn(roles);

        // ACT
        List<RoleResponse> result = roleService.searchRolesByName("CUST");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CUSTOMER", result.get(0).getRoleName());
        verify(roleRepository, times(1)).findByRoleNameContaining("CUST");
    }

    @Test
    @DisplayName("Test 12: Tìm kiếm roles - Không có kết quả")
    void testSearchRolesByName_NoResults() {
        // ARRANGE
        when(roleRepository.findByRoleNameContaining("NOTEXIST")).thenReturn(List.of());

        // ACT
        List<RoleResponse> result = roleService.searchRolesByName("NOTEXIST");

        // ASSERT
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleRepository, times(1)).findByRoleNameContaining("NOTEXIST");
    }
}
