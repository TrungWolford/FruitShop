package server.FruitShop.dto.request.Account;

import lombok.Data;

@Data
public class LoginRequest {
    private String accountPhone;
    private String password;
}