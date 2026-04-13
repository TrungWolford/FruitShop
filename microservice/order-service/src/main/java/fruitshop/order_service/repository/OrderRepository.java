package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByAccountId(String accountId);
}
