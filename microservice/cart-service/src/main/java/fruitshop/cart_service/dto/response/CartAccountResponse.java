package fruitshop.cart_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAccountResponse {
    private String accountId;
    private String accountName;
    private String accountPhone;
    private int status;
}
