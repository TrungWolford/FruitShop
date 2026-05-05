package fruitshop.order_service.service;

import fruitshop.order_service.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface RefundService {
    Refund create(String orderId, String orderItemId, Refund refund);
    List<Refund> findByOrderId(String orderId);
    Refund approveRefund(String orderId, String refundId, String approverName);

    Page<Refund> getAllRefunds(Pageable pageable);
    Refund getRefundById(String refundId);
    Refund updateRefundStatus(String refundId, String status);
    
    Page<Refund> getRefundsByStatus(String status, Pageable pageable);
    List<Refund> getRefundsByOrderItemId(String orderItemId);
    
    Page<Refund> searchRefunds(String keyword, Pageable pageable);
    Page<Refund> getRefundsByDateRange(Instant startDate, Instant endDate, Pageable pageable);
    
    Refund rejectRefund(String refundId);
    Refund completeRefund(String refundId);
    void cancelRefund(String refundId);
    
    long countPendingRefunds();
}
