package fruitshop.cart_service.repository;

import fruitshop.cart_service.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByAccountId(String accountId);
}
