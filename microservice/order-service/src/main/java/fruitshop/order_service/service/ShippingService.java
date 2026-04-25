package fruitshop.order_service.service;

import fruitshop.order_service.entity.Shipping;

public interface ShippingService {
    Shipping upsert(String orderId, Shipping shipping);

    Shipping findByOrderId(String orderId);
}
