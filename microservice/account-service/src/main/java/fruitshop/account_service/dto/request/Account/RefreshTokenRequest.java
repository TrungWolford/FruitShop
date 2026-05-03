package fruitshop.account_service.dto.request.Account;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
