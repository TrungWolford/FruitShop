package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Order.CreateOrderRequest;
import server.FruitShop.dto.request.Order.UpdateOrderRequest;
import server.FruitShop.dto.response.Order.OrderItemResponse;
import server.FruitShop.dto.response.Order.OrderResponse;

import java.util.List;

public interface OrderService {
    // Order operations
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(String orderId);
    OrderResponse updateOrder(String orderId, UpdateOrderRequest request);
    void deleteOrder(String orderId);

    // Account order operations
    List<OrderResponse> getOrdersByAccountId(String accountId);
    Page<OrderResponse> getAllOrders(Pageable pageable);

    // Order status operations
    OrderResponse updateOrderStatus(String orderId, int newStatus);
    OrderResponse cancelOrder(String orderId);
    OrderResponse confirmOrder(String orderId); // Admin xác nhận đơn hàng (status 1 -> 2) và tạo Shipping
    OrderResponse startDelivery(String orderId); // Admin bắt đầu giao hàng (status 2 -> 3)
    OrderResponse completeOrder(String orderId); // Khách hàng xác nhận đã nhận hàng (status 3 -> 4)

    // Filter and search
    Page<OrderResponse> getOrdersByStatus(int status, Pageable pageable);
    Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable);
    Page<OrderResponse> searchOrders(String keyword, Pageable pageable);
    Page<OrderResponse> filterOrdersByStatus(int status, Pageable pageable);
    Page<OrderResponse> searchAndFilterOrders(String keyword, Integer status, Pageable pageable);

    // OrderItem operations
    List<OrderItemResponse> getOrderItems(String orderId);
    List<OrderItemResponse> getOrderDetailsByOrderId(String orderId);
}