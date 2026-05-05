package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.request.Refund.UpdateRefundStatusRequest;
import server.FruitShop.dto.response.Refund.RefundResponse;

import java.util.Date;
import java.util.List;

public interface RefundService {
    
    // Get all refunds with pagination
    Page<RefundResponse> getAllRefunds(Pageable pageable);
    
    // Get refund by ID
    RefundResponse getRefundById(String refundId);
    
    // Create new refund (default status: "Chờ xác nhận")
    RefundResponse createRefund(CreateRefundRequest request);
    
    // Update refund status
    RefundResponse updateRefundStatus(UpdateRefundStatusRequest request, String refundId);
    
    // Get refunds by status
    Page<RefundResponse> getRefundsByStatus(String status, Pageable pageable);
    
    // Get refunds by order ID
    List<RefundResponse> getRefundsByOrderId(String orderId);
    
    // Get refunds by order item ID (per-item refund)
    List<RefundResponse> getRefundsByOrderItemId(String orderItemId);
    
    // Search refunds
    Page<RefundResponse> searchRefunds(String keyword, Pageable pageable);
    
    // Get refunds by date range
    Page<RefundResponse> getRefundsByDateRange(Date startDate, Date endDate, Pageable pageable);
    
    // Approve refund
    RefundResponse approveRefund(String refundId);
    
    // Reject refund
    RefundResponse rejectRefund(String refundId);
    
    // Complete refund
    RefundResponse completeRefund(String refundId);
    
    // Cancel/Delete refund
    void cancelRefund(String refundId);
    
    // Count pending refunds
    long countPendingRefunds();
}
