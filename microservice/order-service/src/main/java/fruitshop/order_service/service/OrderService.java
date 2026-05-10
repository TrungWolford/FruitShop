package fruitshop.order_service.service;

import fruitshop.order_service.dto.request.CreateOrderRequest;
import fruitshop.order_service.dto.request.UpdateOrderRequest;
import fruitshop.order_service.dto.response.OrderItemResponse;
import fruitshop.order_service.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    List<OrderResponse> findByAccountId(String accountId);

    OrderResponse findById(String orderId);

    OrderResponse create(CreateOrderRequest request);

    OrderResponse update(String orderId, UpdateOrderRequest request);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(int status, Pageable pageable);

    Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable);

    Page<OrderResponse> searchOrders(String keyword, Pageable pageable);

    Page<OrderResponse> searchAndFilterOrders(String keyword, Integer status, Pageable pageable);

    List<OrderItemResponse> getOrderItems(String orderId);

    OrderResponse updateOrderStatus(String orderId, int status);

    OrderResponse cancelOrder(String orderId);

    OrderResponse confirmOrder(String orderId);

    OrderResponse startDelivery(String orderId);

    OrderResponse completeOrder(String orderId);
}
