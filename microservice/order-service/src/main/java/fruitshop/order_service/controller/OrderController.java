package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Order>> getByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(orderService.findByAccountId(accountId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.create(order));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItem(@PathVariable String orderId, @RequestBody OrderItem item) {
        return ResponseEntity.ok(orderService.addItem(orderId, item));
    }
}
