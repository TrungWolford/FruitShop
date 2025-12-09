package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.request.Refund.UpdateRefundStatusRequest;
import server.FruitShop.dto.response.Refund.RefundResponse;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.OrderItem;
import server.FruitShop.entity.Payment;
import server.FruitShop.entity.Refund;
import server.FruitShop.exception.ResourceNotFoundException;
import server.FruitShop.repository.OrderRepository;
import server.FruitShop.repository.OrderItemRepository;
import server.FruitShop.repository.PaymentRepository;
import server.FruitShop.repository.RefundRepository;
import server.FruitShop.service.RefundService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RefundServiceImpl implements RefundService {
    
    @Autowired
    private RefundRepository refundRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Override
    public Page<RefundResponse> getAllRefunds(Pageable pageable) {
        Page<Refund> refunds = refundRepository.findAll(pageable);
        return refunds.map(RefundResponse::fromEntity);
    }
    
    @Override
    public RefundResponse getRefundById(String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        return RefundResponse.fromEntity(refund);
    }
    
    @Override
    @Transactional
    public RefundResponse createRefund(CreateRefundRequest request) {
        System.out.println("🔔 Creating refund with request: " + request);
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        System.out.println("🔔 Found order: " + order.getOrderId());
        System.out.println("🔔 Order account: " + (order.getAccount() != null ? order.getAccount().getAccountName() : "NULL"));
        
        // Validate orderItem exists
        OrderItem orderItem = null;
        if (request.getOrderItemId() != null) {
            orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + request.getOrderItemId()));
            
            System.out.println("🔔 Found orderItem: " + orderItem.getOrderDetailId());
            System.out.println("🔔 OrderItem product: " + (orderItem.getProduct() != null ? orderItem.getProduct().getProductName() : "NULL"));
        }
        
        // Get original payment if exists
        Payment originalPayment = null;
        if (request.getOriginalPaymentId() != null) {
            originalPayment = paymentRepository.findById(request.getOriginalPaymentId())
                .orElse(null);
        } else if (order.getPayment() != null) {
            originalPayment = order.getPayment();
        }
        
        // Create refund entity
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setOrderItem(orderItem);  // Set orderItem for per-item refund
        refund.setReason(request.getReason());
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundStatus("Chờ xác nhận"); // Default status
        refund.setRequestedAt(new Date());
        refund.setOriginalPayment(originalPayment);
        
        // Store imageUrls as JSON string
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String imageUrlsJson = mapper.writeValueAsString(request.getImageUrls());
                refund.setImageUrls(imageUrlsJson);
                System.out.println("🔔 Stored imageUrls JSON: " + imageUrlsJson);
            } catch (Exception e) {
                System.err.println("❌ Error converting imageUrls to JSON: " + e.getMessage());
            }
        }
        
        // Save refund
        Refund savedRefund = refundRepository.save(refund);
        
        System.out.println("🔔 Saved refund: " + savedRefund.getRefundId());
        System.out.println("🔔 Saved refund order: " + (savedRefund.getOrder() != null ? savedRefund.getOrder().getOrderId() : "NULL"));
        System.out.println("🔔 Saved refund orderItem: " + (savedRefund.getOrderItem() != null ? savedRefund.getOrderItem().getOrderDetailId() : "NULL"));
        
        RefundResponse response = RefundResponse.fromEntity(savedRefund);
        
        System.out.println("🔔 Response orderId: " + (response.getOrder() != null ? response.getOrder().getOrderId() : "NULL"));
        System.out.println("🔔 Response orderItemId: " + (response.getOrderItem() != null ? response.getOrderItem().getOrderDetailId() : "NULL"));
        System.out.println("🔔 Response refundAmount: " + response.getRefundAmount());
        System.out.println("🔔 Response refundStatus: " + response.getRefundStatus());
        
        return response;
    }
    
    @Override
    @Transactional
    public RefundResponse updateRefundStatus(UpdateRefundStatusRequest request, String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        
        refund.setRefundStatus(request.getRefundStatus());
        
        // If completed, set processed date
        if ("Hoàn thành".equals(request.getRefundStatus()) || "Đã duyệt".equals(request.getRefundStatus())) {
            refund.setProcessedAt(new Date());
        }
        
        Refund updatedRefund = refundRepository.save(refund);
        return RefundResponse.fromEntity(updatedRefund);
    }
    
    @Override
    public Page<RefundResponse> getRefundsByStatus(String status, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findByRefundStatus(status, pageable);
        return refunds.map(RefundResponse::fromEntity);
    }
    
    @Override
    public List<RefundResponse> getRefundsByOrderId(String orderId) {
        List<Refund> refunds = refundRepository.findByOrder_OrderId(orderId);
        return refunds.stream()
            .map(RefundResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<RefundResponse> getRefundsByOrderItemId(String orderItemId) {
        List<Refund> refunds = refundRepository.findByOrderItem_OrderDetailId(orderItemId);
        return refunds.stream()
            .map(RefundResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<RefundResponse> searchRefunds(String keyword, Pageable pageable) {
        Page<Refund> refunds = refundRepository.searchRefunds(keyword, pageable);
        return refunds.map(RefundResponse::fromEntity);
    }
    
    @Override
    public Page<RefundResponse> getRefundsByDateRange(Date startDate, Date endDate, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findByDateRange(startDate, endDate, pageable);
        return refunds.map(RefundResponse::fromEntity);
    }
    
    @Override
    @Transactional
    public RefundResponse approveRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        
        refund.setRefundStatus("Đã duyệt");
        refund.setProcessedAt(new Date());
        
        Refund updatedRefund = refundRepository.save(refund);
        return RefundResponse.fromEntity(updatedRefund);
    }
    
    @Override
    @Transactional
    public RefundResponse rejectRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        
        refund.setRefundStatus("Từ chối");
        refund.setProcessedAt(new Date());
        
        Refund updatedRefund = refundRepository.save(refund);
        return RefundResponse.fromEntity(updatedRefund);
    }
    
    @Override
    @Transactional
    public RefundResponse completeRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        
        refund.setRefundStatus("Hoàn thành");
        refund.setProcessedAt(new Date());
        
        Refund updatedRefund = refundRepository.save(refund);
        return RefundResponse.fromEntity(updatedRefund);
    }
    
    @Override
    @Transactional
    public void cancelRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));
        refundRepository.delete(refund);
    }
    
    @Override
    public long countPendingRefunds() {
        return refundRepository.countByRefundStatus("Chờ xác nhận");
    }
}
