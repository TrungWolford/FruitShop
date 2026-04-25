package fruitshop.account_service.dto.response.Account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private AccountResponse account;
}
