package fruitshop.cart_service.service;

import fruitshop.cart_service.dto.request.CreateCartItemRequest;
import fruitshop.cart_service.dto.request.UpdateCartItemRequest;
import fruitshop.cart_service.dto.response.CartItemResponse;
import fruitshop.cart_service.dto.response.CartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CartService {
    CartResponse getOrCreateCart(String accountId);

    CartResponse addItem(String accountId, CreateCartItemRequest request);

    CartResponse updateItemQuantity(String accountId, String cartItemId, UpdateCartItemRequest request);
    CartResponse updateItemQuantityByItemId(String cartItemId, UpdateCartItemRequest request);
    CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(String accountId, String cartItemId);
    CartResponse removeItemByItemId(String cartItemId);
    void removeCartItem(String cartItemId);

    CartResponse clearCart(String accountId);

    CartResponse updateCartStatus(String cartId, int status);

    CartResponse enableCart(String cartId);

    CartResponse disableCart(String cartId);

    Page<CartResponse> getAllCart(Pageable pageable);

    CartResponse getCartById(String cartId);

    void deleteCart(String cartId);

    List<CartItemResponse> getCartItemsByAccountId(String accountId);
}
