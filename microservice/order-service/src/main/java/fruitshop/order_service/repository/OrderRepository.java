package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByAccountId(String accountId);

    Page<Order> findByAccountId(String accountId, Pageable pageable);

    Page<Order> findByStatus(int status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.accountId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "(LOWER(o.orderId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.accountId) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND o.status = :status")
    Page<Order> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") int status, Pageable pageable);
}
