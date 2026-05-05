package server.FruitShop.service;


import server.FruitShop.dto.request.Role.CreateRoleRequest;
import server.FruitShop.dto.request.Role.UpdateRoleRequest;
import server.FruitShop.dto.response.Role.RoleResponse;

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