package fruitshop.order_service.service.impl;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.exception.DownstreamServiceException;
import fruitshop.order_service.exception.ResourceNotFoundException;
import fruitshop.order_service.feign.AccountClient;
import fruitshop.order_service.feign.ProductClient;
import fruitshop.order_service.feign.dto.AccountSummaryDto;
import fruitshop.order_service.feign.dto.ProductSummaryDto;
import fruitshop.order_service.repository.OrderItemRepository;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import fruitshop.order_service.event.OrderCreatedEvent;
import java.util.stream.Collectors;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AccountClient accountClient;
    private final ProductClient productClient;
    private final StreamBridge streamBridge;

    @Override
    public List<Order> findByAccountId(String accountId) {
        return orderRepository.findByAccountId(accountId);
    }

    @Override
    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Override
    @Transactional
    public Order create(Order order) {
        ensureAccountExists(order.getAccountId());
        order.setStatus(1); // Set initial status (PENDING)
        
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                item.setOrder(order);
                ensureProductExists(item.getProductId());
            });
        }
        
        Order savedOrder = orderRepository.save(order);

        List<OrderCreatedEvent.OrderItemDto> itemDtos = (savedOrder.getOrderItems() == null) ? List.of() :
                savedOrder.getOrderItems().stream().map(i -> new OrderCreatedEvent.OrderItemDto(i.getProductId(), i.getQuantity(), i.getUnitPrice())).collect(Collectors.toList());

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderId(),
                savedOrder.getAccountId(),
                savedOrder.getTotalAmount(),
                itemDtos,
                savedOrder.getCreatedAt()
        );
        streamBridge.send("orderCreatedSupplier-out-0", event);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order addItem(String orderId, OrderItem item) {
        Order order = findById(orderId);
        ensureProductExists(item.getProductId());
        item.setOrder(order);
        orderItemRepository.save(item);
        return orderRepository.findById(orderId).orElse(order);
    }

    private void ensureAccountExists(String accountId) {
        try {
            AccountSummaryDto account = accountClient.getById(accountId);
            if (account == null || account.getAccountId() == null) {
                throw new ResourceNotFoundException("Account not found with id: " + accountId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Account service unavailable while validating accountId: " + accountId);
        }
    }

    private void ensureProductExists(String productId) {
        try {
            ProductSummaryDto product = productClient.getById(productId);
            if (product == null || product.getProductId() == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Catalog service unavailable while validating productId: " + productId);
        }
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(int status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Order> getOrdersByDateRange(String startDate, String endDate, Pageable pageable) {
        try {
            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);
            return orderRepository.findByCreatedAtBetween(start, end, pageable);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Please use ISO-8601 format (e.g. 2023-01-01T00:00:00Z)");
        }
    }

    @Override
    public Page<Order> searchOrders(String keyword, Pageable pageable) {
        return orderRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<Order> searchAndFilterOrders(String keyword, Integer status, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty() && status != null) {
            return orderRepository.searchByKeywordAndStatus(keyword, status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            return orderRepository.searchByKeyword(keyword, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    @Override
    public List<OrderItem> getOrderItems(String orderId) {
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, int status) {
        Order order = findById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId) {
        return updateOrderStatus(orderId, 0); // 0 = Cancelled
    }

    @Override
    @Transactional
    public Order confirmOrder(String orderId) {
        return updateOrderStatus(orderId, 2); // 2 = Confirmed
    }

    @Override
    @Transactional
    public Order startDelivery(String orderId) {
        return updateOrderStatus(orderId, 3); // 3 = Delivering
    }

    @Override
    @Transactional
    public Order completeOrder(String orderId) {
        return updateOrderStatus(orderId, 4); // 4 = Completed
    }
}
