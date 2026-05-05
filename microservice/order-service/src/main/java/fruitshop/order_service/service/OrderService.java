package fruitshop.order_service.service;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    List<Order> findByAccountId(String accountId);

    Order findById(String orderId);

    Order create(Order order);

    Order addItem(String orderId, OrderItem item);

    // --- New: Pagination, Search, Filter, Update ---

    Page<Order> getAllOrders(Pageable pageable);

    Page<Order> getOrdersByStatus(int status, Pageable pageable);

    Page<Order> getOrdersByDateRange(String startDate, String endDate, Pageable pageable);

    Page<Order> searchOrders(String keyword, Pageable pageable);

    Page<Order> searchAndFilterOrders(String keyword, Integer status, Pageable pageable);

    List<OrderItem> getOrderItems(String orderId);

    Order updateOrderStatus(String orderId, int status);

    Order cancelOrder(String orderId);

    Order confirmOrder(String orderId);

    Order startDelivery(String orderId);

    Order completeOrder(String orderId);
}
