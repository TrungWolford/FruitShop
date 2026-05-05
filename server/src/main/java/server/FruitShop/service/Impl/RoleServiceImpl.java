package server.FruitShop.service.Impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Role.CreateRoleRequest;
import server.FruitShop.dto.request.Role.UpdateRoleRequest;
import server.FruitShop.dto.response.Role.RoleResponse;
import server.FruitShop.entity.Role;
import server.FruitShop.repository.RoleRepository;
import server.FruitShop.service.RoleService;
import server.FruitShop.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return RoleResponse.fromEntity(role);
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        Role role = new Role();
        role.setRoleName(request.getRoleName());

        Role savedRole = roleRepository.save(role);
        return RoleResponse.fromEntity(savedRole);
    }

    @Override
    public RoleResponse updateRole(String roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        role.setRoleName(request.getRoleName());

        Role updatedRole = roleRepository.save(role);
        return RoleResponse.fromEntity(updatedRole);
    }

    @Override
    public void deleteRole(String roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public RoleResponse getRoleByName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
        return RoleResponse.fromEntity(role);
    }

    @Override
    public List<RoleResponse> searchRolesByName(String roleName) {
        return roleRepository.findByRoleNameContaining(roleName).stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }
}