package fruitshop.account_service.service;


import fruitshop.account_service.dto.request.Role.CreateRoleRequest;
import fruitshop.account_service.dto.request.Role.UpdateRoleRequest;
import fruitshop.account_service.dto.response.Role.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleById(String roleId);
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse updateRole(String roleId, UpdateRoleRequest request);
    void deleteRole(String roleId);
    RoleResponse getRoleByName(String roleName);
    List<RoleResponse> searchRolesByName(String roleName);
}