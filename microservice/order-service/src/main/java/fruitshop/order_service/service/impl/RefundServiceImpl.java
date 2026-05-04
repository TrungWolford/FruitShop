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
}
