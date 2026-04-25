package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingRepository extends JpaRepository<Shipping, String> {
    Optional<Shipping> findByOrderOrderId(String orderId);
}
