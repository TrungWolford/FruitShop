package fruitshop.order_service.repository;

import fruitshop.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrderOrderId(String orderId);
}
