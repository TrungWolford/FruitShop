package fruitshop.account_service.dto.response.Account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    private String tokenType;
    private String accessToken;
    private long expiresIn;
}
