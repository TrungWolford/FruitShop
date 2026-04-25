package fruitshop.order_service.service.impl;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.ShippingRepository;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    @Override
    public Shipping upsert(String orderId, Shipping shipping) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Shipping existing = shippingRepository.findByOrderOrderId(orderId).orElse(null);
        if (existing != null) {
            existing.setAccountId(shipping.getAccountId());
            existing.setReceiverName(shipping.getReceiverName());
            existing.setReceiverPhone(shipping.getReceiverPhone());
            existing.setReceiverAddress(shipping.getReceiverAddress());
            existing.setCity(shipping.getCity());
            existing.setShipperName(shipping.getShipperName());
            existing.setShippingFee(shipping.getShippingFee());
            existing.setShippedAt(shipping.getShippedAt());
            existing.setStatus(shipping.getStatus());
            return shippingRepository.save(existing);
        }

        shipping.setOrder(order);
        return shippingRepository.save(shipping);
    }

    @Override
    public Shipping findByOrderId(String orderId) {
        return shippingRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found for order: " + orderId));
    }
}
