package fruitshop.cart_service.service;

import fruitshop.cart_service.entity.Cart;
import fruitshop.cart_service.entity.CartItem;

public interface CartService {
    Cart getOrCreateCart(String accountId);

    Cart addItem(String accountId, String productId, int quantity);

    Cart updateItemQuantity(String accountId, String cartItemId, int quantity);

    Cart removeItem(String accountId, String cartItemId);

    Cart clearCart(String accountId);
}
