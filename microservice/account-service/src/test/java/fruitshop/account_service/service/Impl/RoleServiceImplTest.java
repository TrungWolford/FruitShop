package fruitshop.account_service.service.Impl;

import fruitshop.account_service.dto.request.Role.CreateRoleRequest;
import fruitshop.account_service.entity.Role;
import fruitshop.account_service.exception.ResourceNotFoundException;
import fruitshop.account_service.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRole_savesAndReturnsRoleResponse() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("ADMIN");

        Role saved = new Role();
        saved.setRoleId("r-1");
        saved.setRoleName("ADMIN");

        when(roleRepository.save(any(Role.class))).thenReturn(saved);

        var response = roleService.createRole(request);

        assertEquals("r-1", response.getRoleId());
        assertEquals("ADMIN", response.getRoleName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getRoleById_notFound_throwsResourceNotFound() {
        when(roleRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById("missing"));
    }

    @Test
    void deleteRole_notFound_throwsAndDoesNotDelete() {
        when(roleRepository.existsById("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole("missing"));
        verify(roleRepository, never()).deleteById("missing");
    }
}
