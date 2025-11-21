package server.FruitShop.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Cart.CartResponse;

import java.util.List;

public interface CartService {
    // Cart operations
    Page<CartResponse> getAllCart(Pageable pageable);
    CartResponse getCartByAccountId(String accountId);
    CartResponse createCart(String accountId);
    void deleteCart(String cartId);
    CartResponse disableCart(String cartId); // Admin vô hiệu hóa giỏ hàng
    CartResponse enableCart(String cartId);  // Admin kích hoạt lại giỏ hàng
    CartResponse updateCartStatus(String cartId, int status); // Cập nhật trạng thái giỏ hàng

    // CartItem operations
    CartItemResponse addCartItem(String accountId, CreateCartItemRequest request);
    CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest  request);
    void removeCartItem(String cartItemId);
    List<CartItemResponse> getCartItemsByAccountId(String accountId);
    void clearCart(String accountId);
}
