package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Order;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByAccountAccountId(String accountId);
    Page<Order> findByStatus(int status, Pageable pageable);
    Page<Order> findByCreatedAtBetween(Date startDate, Date endDate, Pageable pageable);
    
    // Search methods
    Page<Order> findByOrderIdContainingIgnoreCaseOrAccountAccountNameContainingIgnoreCase(
            String orderId, String accountName, Pageable pageable);
    
    // Combined search and filter
    Page<Order> findByOrderIdContainingIgnoreCaseAndStatusOrAccountAccountNameContainingIgnoreCaseAndStatus(
            String orderId, int status1, String accountName, int status2, Pageable pageable);
}