package fruitshop.order_service.service.impl;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.entity.Refund;
import fruitshop.order_service.event.RefundApprovedEvent;
import fruitshop.order_service.repository.OrderItemRepository;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.RefundRepository;
import fruitshop.order_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StreamBridge streamBridge;

    @Override
    public Refund create(String orderId, String orderItemId, Refund refund) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        refund.setOrder(order);

        if (orderItemId != null && !orderItemId.isBlank()) {
            OrderItem item = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));
            refund.setOrderItem(item);
        }
        
        refund.setRefundStatus("PENDING");
        refund.setRequestedAt(Instant.now());

        return refundRepository.save(refund);
    }

    @Override
    public List<Refund> findByOrderId(String orderId) {
        return refundRepository.findByOrderOrderId(orderId);
    }

    @Override
    public Refund approveRefund(String orderId, String refundId, String approverName) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

        if (!refund.getOrder().getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("Refund does not belong to order: " + orderId);
        }

        refund.setRefundStatus("APPROVED");
        Refund savedRefund = refundRepository.save(refund);

        // Publish RefundApprovedEvent
        RefundApprovedEvent event = new RefundApprovedEvent(
                savedRefund.getRefundId(),
                orderId,
                savedRefund.getRefundAmount(),
                approverName,
                new Date()
            );
        streamBridge.send("refundApprovedSupplier-out-0", event);
        log.info("Published RefundApprovedEvent for refund: {}", refundId);

        return savedRefund;
    }

    @Override
    public Page<Refund> getAllRefunds(Pageable pageable) {
        return refundRepository.findAll(pageable);
    }

    @Override
    public Refund getRefundById(String refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));
    }

    @Override
    public Refund updateRefundStatus(String refundId, String status) {
        Refund refund = getRefundById(refundId);
        refund.setRefundStatus(status);
        if ("COMPLETED".equals(status) || "FAILED".equals(status) || "REJECTED".equals(status)) {
            refund.setProcessedAt(Instant.now());
        }
        return refundRepository.save(refund);
    }

    @Override
    public Page<Refund> getRefundsByStatus(String status, Pageable pageable) {
        return refundRepository.findByRefundStatus(status, pageable);
    }

    @Override
    public List<Refund> getRefundsByOrderItemId(String orderItemId) {
        return refundRepository.findByOrderItemOrderItemId(orderItemId);
    }

    @Override
    public Page<Refund> searchRefunds(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return refundRepository.findAll(pageable);
        }
        return refundRepository.searchRefunds(keyword, pageable);
    }

    @Override
    public Page<Refund> getRefundsByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        return refundRepository.findByRequestedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public Refund rejectRefund(String refundId) {
        return updateRefundStatus(refundId, "REJECTED");
    }

    @Override
    public Refund completeRefund(String refundId) {
        return updateRefundStatus(refundId, "COMPLETED");
    }

    @Override
    public void cancelRefund(String refundId) {
        updateRefundStatus(refundId, "CANCELLED");
    }

    @Override
    public long countPendingRefunds() {
        return refundRepository.countByRefundStatus("PENDING");
    }
}
