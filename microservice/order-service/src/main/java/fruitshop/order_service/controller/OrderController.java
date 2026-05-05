package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  // --- New features ---

  @GetMapping
  public ResponseEntity<Page<Order>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(orderService.getAllOrders(pageable));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<Order>> getOrdersByStatus(
      @PathVariable int status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
  }

  @GetMapping("/daterange")
  public ResponseEntity<Page<Order>> getOrdersByDateRange(
      @RequestParam String startDate,
      @RequestParam String endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate, pageable));
  }

  @GetMapping("/search")
  public ResponseEntity<Page<Order>> searchAndFilterOrders(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(orderService.searchAndFilterOrders(keyword, status, pageable));
  }

  @PutMapping("/{orderId}/status")
  public ResponseEntity<Order> updateOrderStatus(
      @PathVariable String orderId,
      @RequestParam int status) {
    return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
  }

  @GetMapping("/{orderId}/items")
  public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.getOrderItems(orderId));
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.cancelOrder(orderId));
  }

  @PutMapping("/{orderId}/confirm")
  public ResponseEntity<Order> confirmOrder(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.confirmOrder(orderId));
  }

  @PutMapping("/{orderId}/start-delivery")
  public ResponseEntity<Order> startDelivery(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.startDelivery(orderId));
  }

  @PutMapping("/{orderId}/complete")
  public ResponseEntity<Order> completeOrder(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.completeOrder(orderId));
  }
}
