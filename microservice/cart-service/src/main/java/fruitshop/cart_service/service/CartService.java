package fruitshop.cart_service.service;

import fruitshop.cart_service.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartService {
    Cart getOrCreateCart(String accountId);

    Cart addItem(String accountId, String productId, int quantity);

    Cart updateItemQuantity(String accountId, String cartItemId, int quantity);

    Cart removeItem(String accountId, String cartItemId);

    Cart clearCart(String accountId);

    Cart updateCartStatus(String cartId, int status);

    Cart enableCart(String cartId);

    Cart disableCart(String cartId);

    Page<Cart> getAllCart(Pageable pageable);

    Cart getCartById(String cartId);

    void deleteCart(String cartId);
}

