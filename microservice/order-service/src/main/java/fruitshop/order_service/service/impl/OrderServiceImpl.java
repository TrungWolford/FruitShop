package fruitshop.order_service.service.impl;

import fruitshop.order_service.dto.request.CreateOrderRequest;
import fruitshop.order_service.dto.request.UpdateOrderRequest;
import fruitshop.order_service.dto.response.OrderItemResponse;
import fruitshop.order_service.dto.response.OrderResponse;
import fruitshop.order_service.feign.dto.PaymentResponse;
import fruitshop.order_service.dto.response.ShippingResponse;
import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.OrderItem;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.exception.DownstreamServiceException;
import fruitshop.order_service.exception.ResourceNotFoundException;
import fruitshop.order_service.feign.AccountClient;
import fruitshop.order_service.feign.PaymentClient;
import fruitshop.order_service.feign.ProductClient;
import fruitshop.order_service.feign.dto.AccountSummaryDto;
import fruitshop.order_service.feign.dto.ProductSummaryDto;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.service.OrderService;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import fruitshop.order_service.event.OrderCreatedEvent;
import fruitshop.order_service.event.OrderConfirmedEvent;
import java.time.Instant;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final AccountClient accountClient;
  private final ProductClient productClient;
  private final PaymentClient paymentClient;
  private final ShippingService shippingService;
  private final StreamBridge streamBridge;

  @Override
  public List<OrderResponse> findByAccountId(String accountId) {
    AccountSummaryDto accountDto = null;
    try {
      accountDto = accountClient.getById(accountId);
    } catch (Exception ignored) {
    }

    final AccountSummaryDto finalAccountDto = accountDto;
    return orderRepository.findByAccountId(accountId).stream()
        .map(order -> mapToResponse(order, finalAccountDto))
        .collect(Collectors.toList());
  }

  @Override
  public OrderResponse findById(String orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

    AccountSummaryDto accountDto = null;
    try {
      accountDto = accountClient.getById(order.getAccountId());
    } catch (Exception ignored) {
    }

    return mapToResponse(order, accountDto);
  }

  @Override
  @Transactional
  public OrderResponse create(CreateOrderRequest request) {
    AccountSummaryDto accountDto = ensureAccountExists(request.getAccountId());

    Order order = new Order();
    order.setAccountId(request.getAccountId());
    order.setStatus(1); // Chờ xác nhận

    // Handle Payment Method mapping from Monolith
    if (request.getPaymentMethod() != null) {
      String method = switch (request.getPaymentMethod()) {
        case 0 -> "COD";
        case 1 -> "BANK_TRANSFER";
        default -> "OTHER";
      };
      // Note: In microservice, the actual payment record creation is handled via Saga
      // or Payment Service.
      // But we can store the intended method or pass it in the event.
      log.info("Order created with payment method: {}", method);
    }

    List<OrderItem> items = new ArrayList<>();
    long subtotal = 0;
    if (request.getItems() != null) {
      for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
        ProductSummaryDto product = ensureProductExistsAndGet(itemReq.getProductId());
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(itemReq.getProductId());
        item.setQuantity(itemReq.getQuantity());
        // Use product price from catalog if unitPrice not provided (monolith behavior)
        long unitPrice = (itemReq.getUnitPrice() > 0) ? itemReq.getUnitPrice() : product.getPrice();
        item.setUnitPrice(unitPrice);
        items.add(item);
        subtotal += unitPrice * itemReq.getQuantity();
      }
    }
    order.setOrderItems(items);

    // Handle Shipping Template Copy (Monolith fallback logic)
    long shippingFee = 0;
    Shipping shipping = null;

    if (request.getShippingId() != null) {
      shipping = shippingService.getRawEntityById(request.getShippingId());
    }

    // Monolith fallback: if shippingId not found, try to get latest account
    // template
    if (shipping == null && request.getAccountId() != null) {
      log.warn("Shipping template not found with id: {} - attempting account fallback", request.getShippingId());
      List<ShippingResponse> accountShippings = shippingService.getShippingsByAccountId(request.getAccountId());
      if (!accountShippings.isEmpty()) {
        // Get the latest template
        ShippingResponse template = accountShippings.get(accountShippings.size() - 1);

        Shipping newShipping = new Shipping();
        newShipping.setOrder(order);
        newShipping.setAccountId(request.getAccountId());
        newShipping.setReceiverName(template.getReceiverName());
        newShipping.setReceiverPhone(template.getReceiverPhone());
        newShipping.setReceiverAddress(template.getReceiverAddress());
        newShipping.setCity(template.getCity());
        newShipping.setShippingFee(template.getShippingFee());
        newShipping.setStatus(1); // Chờ xác nhận
        order.setShipping(newShipping);
        shippingFee = template.getShippingFee();
      }
    } else if (shipping != null) {
      Shipping newShipping = new Shipping();
      newShipping.setOrder(order);
      newShipping.setAccountId(request.getAccountId());
      newShipping.setReceiverName(shipping.getReceiverName());
      newShipping.setReceiverPhone(shipping.getReceiverPhone());
      newShipping.setReceiverAddress(shipping.getReceiverAddress());
      newShipping.setCity(shipping.getCity());
      newShipping.setShippingFee(shipping.getShippingFee());
      newShipping.setStatus(1); // Chờ xác nhận
      order.setShipping(newShipping);
      shippingFee = shipping.getShippingFee();
    } else if (request.getShipping() != null) {
      Shipping newShipping = new Shipping();
      newShipping.setOrder(order);
      newShipping.setAccountId(request.getAccountId());
      newShipping.setReceiverName(request.getShipping().getReceiverName());
      newShipping.setReceiverPhone(request.getShipping().getReceiverPhone());
      newShipping.setReceiverAddress(request.getShipping().getReceiverAddress());
      newShipping.setCity(request.getShipping().getCity());
      newShipping.setShippingFee(request.getShipping().getShippingFee());
      newShipping.setStatus(1);
      order.setShipping(newShipping);
      shippingFee = request.getShipping().getShippingFee();
    }

    order.setTotalAmount(subtotal + shippingFee);

    Order savedOrder = orderRepository.save(order);

    // Send OrderCreatedEvent
    List<OrderCreatedEvent.OrderItemDto> itemEventDtos = savedOrder.getOrderItems().stream()
        .map(i -> new OrderCreatedEvent.OrderItemDto(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
        .collect(Collectors.toList());

    OrderCreatedEvent event = new OrderCreatedEvent(
        savedOrder.getOrderId(),
        savedOrder.getAccountId(),
        savedOrder.getTotalAmount(),
        request.getPaymentMethod() != null ? request.getPaymentMethod() : 0,
        itemEventDtos,
        savedOrder.getCreatedAt());
    streamBridge.send("orderCreatedSupplier-out-0", event);

    // COD orders: confirm immediately since no online payment needed
    // This triggers stock deduction in catalog-service
    Integer paymentMethod = request.getPaymentMethod();
    if (paymentMethod == null || paymentMethod == 0) {
      try {
        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
            savedOrder.getOrderId(),
            savedOrder.getAccountId(),
            savedOrder.getOrderItems().stream()
                .map(item -> new OrderConfirmedEvent.OrderItemDto(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList()),
            Instant.now());
        streamBridge.send("orderConfirmedSupplier-out-0", confirmedEvent);
        log.info("COD order {} confirmed immediately, stock deduction triggered", savedOrder.getOrderId());
      } catch (Exception e) {
        log.error("Failed to publish OrderConfirmedEvent for COD order: {}", savedOrder.getOrderId(), e);
      }
    }

    return mapToResponse(savedOrder, accountDto);
  }

  @Override
  @Transactional
  public OrderResponse update(String orderId, UpdateOrderRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

    int oldStatus = order.getStatus();
    order.setStatus(request.getStatus());
    Order savedOrder = orderRepository.save(order);

    // Sync Shipping and Payment status automatically
    handleStatusSync(savedOrder, request.getStatus(), oldStatus);

    AccountSummaryDto accountDto = null;
    try {
      accountDto = accountClient.getById(order.getAccountId());
    } catch (Exception ignored) {
    }

    return mapToResponse(savedOrder, accountDto);
  }

  private void handleStatusSync(Order order, int newStatus, int oldStatus) {
    // Sync Shipping
    try {
      Shipping shipping = order.getShipping();
      if (shipping != null) {
        if (newStatus == 2 && oldStatus == 1) {
          shipping.setStatus(2); // Chờ giao hàng
        } else if (newStatus == 3) {
          shipping.setStatus(3); // Đang giao
          shipping.setShippedAt(Instant.now());
        } else if (newStatus == 4) {
          shipping.setStatus(4); // Đã giao
        } else if (newStatus == 0) {
          shipping.setStatus(0); // Đã hủy
        }
      }
    } catch (Exception ignored) {
    }

    // Sync Payment
    try {
      if (newStatus == 4 && order.getPaymentId() != null) {
        paymentClient.updateStatus(order.getPaymentId(), 1); // 1 = COMPLETED
      } else if (newStatus == 0 && order.getPaymentId() != null) {
        paymentClient.updateStatus(order.getPaymentId(), 2); // 2 = FAILED/CANCELLED
      }
    } catch (Exception ignored) {
    }
  }

  private AccountSummaryDto ensureAccountExists(String accountId) {
    try {
      AccountSummaryDto account = accountClient.getById(accountId);
      if (account == null || account.getAccountId() == null) {
        throw new ResourceNotFoundException("Account not found with id: " + accountId);
      }
      return account;
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

  private ProductSummaryDto ensureProductExistsAndGet(String productId) {
    try {
      ProductSummaryDto product = productClient.getById(productId);
      if (product == null || product.getProductId() == null) {
        throw new ResourceNotFoundException("Product not found with id: " + productId);
      }
      return product;
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new DownstreamServiceException("Catalog service unavailable while validating productId: " + productId);
    }
  }

  private OrderResponse mapToResponse(Order order, AccountSummaryDto accountDto) {
    if (order == null)
      return null;

    List<OrderItemResponse> itemResponses = new ArrayList<>();
    if (order.getOrderItems() != null) {
      for (OrderItem item : order.getOrderItems()) {
        OrderItemResponse itemDto = OrderItemResponse.builder()
            .orderDetailId(item.getOrderItemId())
            .productId(item.getProductId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getUnitPrice() * item.getQuantity())
            .build();

        try {
          ProductSummaryDto product = productClient.getById(item.getProductId());
          if (product != null) {
            itemDto.setProductName(product.getProductName());
            if (product.getImages() != null) {
                itemDto.setProductImages(product.getImages().stream()
                        .map(ProductSummaryDto.ProductImageDto::getImageUrl)
                        .collect(Collectors.toList()));
            }
          }
        } catch (Exception ignored) {
        }
        itemResponses.add(itemDto);
      }
    }

    OrderResponse response = OrderResponse.builder()
        .orderId(order.getOrderId())
        .accountId(order.getAccountId())
        .createdAt(order.getCreatedAt())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .orderItems(itemResponses)
        .totalItems(itemResponses.size())
        .build();

    if (accountDto != null) {
      response.setAccountName(accountDto.getAccountName());
    }

    // Fetch Shipping info
    try {
      ShippingResponse shipping = shippingService.findByOrderId(order.getOrderId());
      response.setShipping(shipping);
    } catch (Exception ignored) {
    }

    // Fetch Payment info
    if (order.getPaymentId() != null) {
      try {
        PaymentResponse payment = paymentClient.getByPaymentId(order.getPaymentId());
        response.setPayment(payment);
      } catch (Exception ignored) {
      }
    }

    return response;
  }

  @Override
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    return orderRepository.findAll(pageable).map(order -> {
      AccountSummaryDto accountDto = null;
      try {
        accountDto = accountClient.getById(order.getAccountId());
      } catch (Exception ignored) {
      }
      return mapToResponse(order, accountDto);
    });
  }

  @Override
  public Page<OrderResponse> getOrdersByStatus(int status, Pageable pageable) {
    return orderRepository.findByStatus(status, pageable).map(order -> {
      AccountSummaryDto accountDto = null;
      try {
        accountDto = accountClient.getById(order.getAccountId());
      } catch (Exception ignored) {
      }
      return mapToResponse(order, accountDto);
    });
  }

  @Override
  public Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable) {
    try {
      Instant start = Instant.parse(startDate);
      Instant end = Instant.parse(endDate);
      return orderRepository.findByCreatedAtBetween(start, end, pageable).map(order -> {
        AccountSummaryDto accountDto = null;
        try {
          accountDto = accountClient.getById(order.getAccountId());
        } catch (Exception ignored) {
        }
        return mapToResponse(order, accountDto);
      });
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date format. Please use ISO-8601 format (e.g. 2023-01-01T00:00:00Z)");
    }
  }

  @Override
  public Page<OrderResponse> searchOrders(String keyword, Pageable pageable) {
    return orderRepository.searchByKeyword(keyword, pageable).map(order -> {
      AccountSummaryDto accountDto = null;
      try {
        accountDto = accountClient.getById(order.getAccountId());
      } catch (Exception ignored) {
      }
      return mapToResponse(order, accountDto);
    });
  }

  @Override
  public Page<OrderResponse> searchAndFilterOrders(String keyword, Integer status, Pageable pageable) {
    Page<Order> orders;
    if (keyword != null && !keyword.trim().isEmpty() && status != null) {
      orders = orderRepository.searchByKeywordAndStatus(keyword, status, pageable);
    } else if (keyword != null && !keyword.trim().isEmpty()) {
      orders = orderRepository.searchByKeyword(keyword, pageable);
    } else if (status != null) {
      orders = orderRepository.findByStatus(status, pageable);
    } else {
      orders = orderRepository.findAll(pageable);
    }

    return orders.map(order -> {
      AccountSummaryDto accountDto = null;
      try {
        accountDto = accountClient.getById(order.getAccountId());
      } catch (Exception ignored) {
      }
      return mapToResponse(order, accountDto);
    });
  }

  @Override
  public List<OrderItemResponse> getOrderItems(String orderId) {
    OrderResponse orderResponse = findById(orderId);
    return orderResponse.getOrderItems();
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(String orderId, int status) {
    UpdateOrderRequest request = new UpdateOrderRequest();
    request.setStatus(status);
    return update(orderId, request);
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(String orderId) {
    return updateOrderStatus(orderId, 0); // 0 = Cancelled
  }

  @Override
  @Transactional
  public OrderResponse confirmOrder(String orderId) {
    return updateOrderStatus(orderId, 2); // 2 = Confirmed
  }

  @Override
  @Transactional
  public OrderResponse startDelivery(String orderId) {
    return updateOrderStatus(orderId, 3); // 3 = Delivering
  }

  @Override
  @Transactional
  public OrderResponse completeOrder(String orderId) {
    return updateOrderStatus(orderId, 4); // 4 = Completed
  }
}
