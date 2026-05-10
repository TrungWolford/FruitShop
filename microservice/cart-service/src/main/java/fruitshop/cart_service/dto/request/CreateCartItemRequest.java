package fruitshop.cart_service.dto.request;

import lombok.Data;

@Data
public class CreateCartItemRequest {
    private String productId;
    private int quantity;
}
