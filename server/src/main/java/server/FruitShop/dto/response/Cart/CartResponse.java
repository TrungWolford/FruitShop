package server.FruitShop.dto.response.Cart;
import lombok.Data;
import server.FruitShop.entity.Cart;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private String cartId;
    private String accountId;
    private String accountName;
    private List<CartItemResponse> items;
    private int totalItems;
    private long totalPrice;
    private Date createdAt;

    public static CartResponse fromEntity(Cart cart){
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setCreatedAt(cart.getCreatedAt());

        if (cart.getAccount() != null) {
            response.setAccountId(cart.getAccount().getAccountId());
            response.setAccountName(cart.getAccount().getAccountName());
        }

        if (cart.getItems() != null) {
            List<CartItemResponse> itemResponses = cart.getItems().stream()
                    .map(CartItemResponse::fromEntity)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);

            response.setTotalItems(cart.getItems().size());
            response.setTotalPrice(cart.getItems().stream()
                    .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum());
        }

        return response;
    }
}