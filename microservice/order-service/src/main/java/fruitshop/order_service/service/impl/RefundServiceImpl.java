package fruitshop.order_service.service.impl;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.entity.Refund;
import fruitshop.order_service.repository.OrderItemRepository;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.RefundRepository;
import fruitshop.order_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

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

        return refundRepository.save(refund);
    }

    @Override
    public List<Refund> findByOrderId(String orderId) {
        return refundRepository.findByOrderOrderId(orderId);
    }
}
