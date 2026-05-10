package fruitshop.cart_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String cartId;
    private String accountId;
    private String accountName;
    private CartAccountResponse account;
    private List<CartItemResponse> items;
    private int totalItems;
    private long totalPrice;
    private Instant createdAt;
    private int status;
    private String statusText;
}
