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

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AccountClient accountClient;
    private final ProductClient productClient;

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
    public Order create(Order order) {
        ensureAccountExists(order.getAccountId());
        return orderRepository.save(order);
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
}
