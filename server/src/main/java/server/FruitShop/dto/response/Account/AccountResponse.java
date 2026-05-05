package server.FruitShop.dto.response.Account;

import lombok.Data;
import server.FruitShop.dto.response.Role.RoleResponse;
import server.FruitShop.entity.Account;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AccountResponse {
    private String accountId;
    private String accountName;
    private String accountPhone;
    private String password;
    private int status;
    private Set<RoleResponse> roles;

    public static AccountResponse fromEntity(Account account){
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountName(account.getAccountName());
        response.setAccountPhone(account.getAccountPhone());
        response.setPassword(account.getPassword());
        response.setStatus(account.getStatus());
        response.setRoles(account.getRoles()
                .stream().map(RoleResponse::fromEntity)
                .collect(Collectors.toSet()));

        return response;
    }
}