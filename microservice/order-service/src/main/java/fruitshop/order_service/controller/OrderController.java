package fruitshop.order_service.controller;

import fruitshop.order_service.dto.request.CreateOrderRequest;
import fruitshop.order_service.dto.request.UpdateOrderRequest;
import fruitshop.order_service.dto.response.OrderItemResponse;
import fruitshop.order_service.dto.response.OrderResponse;
import fruitshop.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(orderService.findByAccountId(accountId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @RequestBody UpdateOrderRequest request
    ) {
        return ResponseEntity.ok(orderService.update(orderId, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable int status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @GetMapping("/items/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderItems(orderId));
    }

    // Customer endpoints
    // @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.completeOrder(orderId));
    }

    // Admin endpoints
    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.confirmOrder(orderId));
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/start-delivery")
    public ResponseEntity<OrderResponse> startDelivery(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.startDelivery(orderId));
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/update-status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam int status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
