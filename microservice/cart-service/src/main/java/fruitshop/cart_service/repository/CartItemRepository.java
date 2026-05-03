package fruitshop.cart_service.repository;

import fruitshop.cart_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByCartCartId(String cartId);

    Optional<CartItem> findByCartCartIdAndProductId(String cartId, String productId);
}
