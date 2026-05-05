package server.FruitShop.dto.response.Role;

import lombok.Data;
import server.FruitShop.entity.Role;

@Data
public class RoleResponse {
    private String roleId;
    private String roleName;

    public static RoleResponse fromEntity(Role role){
        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getRoleId());
        response.setRoleName(role.getRoleName());

        return response;
    }
}