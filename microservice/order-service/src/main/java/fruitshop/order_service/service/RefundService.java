package fruitshop.order_service.service;

import fruitshop.order_service.entity.Refund;

import java.util.List;

public interface RefundService {
    Refund create(String orderId, String orderItemId, Refund refund);

    List<Refund> findByOrderId(String orderId);
}
