package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Refund;

import java.util.Date;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {
    
    // Find all refunds with pagination
    Page<Refund> findAll(Pageable pageable);
    
    // Find refunds by status
    Page<Refund> findByRefundStatus(String refundStatus, Pageable pageable);
    
    // Find refunds by order ID
    List<Refund> findByOrder_OrderId(String orderId);
    
    // Find refund by order item ID (per-item refund)
    List<Refund> findByOrderItem_OrderDetailId(String orderDetailId);
    
    // Find refunds by date range
    @Query("SELECT r FROM Refund r WHERE r.requestedAt BETWEEN :startDate AND :endDate")
    Page<Refund> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);
    
    // Search refunds (by order ID or reason)
    @Query("SELECT r FROM Refund r WHERE " +
           "LOWER(r.order.orderId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Refund> searchRefunds(@Param("keyword") String keyword, Pageable pageable);
    
    // Count refunds by status
    long countByRefundStatus(String refundStatus);
    
    // Find pending refunds
    @Query("SELECT r FROM Refund r WHERE r.refundStatus = 'Chờ xác nhận' ORDER BY r.requestedAt DESC")
    Page<Refund> findPendingRefunds(Pageable pageable);
}
