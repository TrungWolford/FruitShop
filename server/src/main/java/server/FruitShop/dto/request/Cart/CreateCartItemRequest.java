package server.FruitShop.dto.request.Cart;

import lombok.Data;

@Data
public class CreateCartItemRequest {
    private String productId;
    private int quantity;
}
