package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {
    List<Refund> findByOrderOrderId(String orderId);
}
