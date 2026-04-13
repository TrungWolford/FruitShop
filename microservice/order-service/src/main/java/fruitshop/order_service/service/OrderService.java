package fruitshop.order_service.service;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;

import java.util.List;

public interface OrderService {
    List<Order> findByAccountId(String accountId);

    Order findById(String orderId);

    Order create(Order order);

    Order addItem(String orderId, OrderItem item);
}
