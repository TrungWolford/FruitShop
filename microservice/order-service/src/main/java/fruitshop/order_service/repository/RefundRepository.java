package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {
    List<Refund> findByOrderOrderId(String orderId);
    List<Refund> findByOrderItemOrderItemId(String orderItemId);
    Page<Refund> findByRefundStatus(String status, Pageable pageable);
    Page<Refund> findByRequestedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    long countByRefundStatus(String status);

    @Query("SELECT r FROM Refund r WHERE LOWER(r.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.refundId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Refund> searchRefunds(@Param("keyword") String keyword, Pageable pageable);
}
