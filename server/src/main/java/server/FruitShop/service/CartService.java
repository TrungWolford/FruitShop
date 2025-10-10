package server.FruitShop.service;


import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Cart.CartResponse;

import java.util.List;

public interface CartService {
    // Cart operations
    CartResponse getCartByAccountId(String accountId);
    CartResponse createCart(String accountId);
    void deleteCart(String cartId);

    // CartItem operations
    CartItemResponse addCartItem(String accountId, CreateCartItemRequest request);
    CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest  request);
    void removeCartItem(String cartItemId);
    List<CartItemResponse> getCartItemsByAccountId(String accountId);
    void clearCart(String accountId);
}
