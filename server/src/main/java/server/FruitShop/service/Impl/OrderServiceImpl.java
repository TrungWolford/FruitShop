package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Order.CreateOrderRequest;
import server.FruitShop.dto.request.Order.UpdateOrderRequest;
import server.FruitShop.dto.response.Order.OrderItemResponse;
import server.FruitShop.dto.response.Order.OrderResponse;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;
import server.FruitShop.service.OrderService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        System.out.println("🛒 Creating order for accountId: " + request.getAccountId());

        // Validate account
        Optional<Account> accountOptional = accountRepository.findById(request.getAccountId());
        if (accountOptional.isEmpty()) {
            throw new RuntimeException("Account not found with id: " + request.getAccountId());
        }

        System.out.println("🛒 Account found: " + accountOptional.get().getAccountName());

        // Validate shipping
        Optional<Shipping> shippingOptional = shippingRepository.findById(request.getShippingId());
        if (shippingOptional.isEmpty()) {
            throw new RuntimeException("Shipping not found with id: " + request.getShippingId());
        }

        System.out.println("🛒 Shipping found: " + shippingOptional.get().getReceiverName());

        // Validate or create payment
        Payment paymentEntity = null;
        if (request.getPaymentId() != null) {
            Optional<Payment> paymentOptional = paymentRepository.findById(request.getPaymentId());
            if (paymentOptional.isEmpty()) {
                throw new RuntimeException("Payment not found with id: " + request.getPaymentId());
            }
            paymentEntity = paymentOptional.get();
            System.out.println("🛒 Payment found: " + paymentEntity.getPaymentId());
        } else if (request.getPaymentMethod() != null) {
            // Create a Payment record based on numeric paymentMethod
            Payment p = new Payment();
            String method = switch (request.getPaymentMethod()) {
                case 0 -> "COD";
                case 1 -> "BANK_TRANSFER";
                default -> "OTHER";
            };
            p.setPaymentMethod(method);
            p.setPaymentStatus(0); // pending
            p.setPaymentDate(new java.util.Date());
            p.setAmount(java.math.BigDecimal.ZERO);
            paymentEntity = paymentRepository.save(p);
            System.out.println("🛒 Created payment: " + paymentEntity.getPaymentId() + " method=" + method);
        } else {
            throw new RuntimeException("Payment information missing");
        }

        // Create order
    Order order = new Order();
        order.setAccount(accountOptional.get());
        // To avoid potential DB unique constraints on existing shippingId,
        // clone the Shipping into a new Shipping record attached to this order.
        Shipping existingShipping = shippingOptional.get();
        Shipping shippingClone = new Shipping();
        shippingClone.setAccount(existingShipping.getAccount());
        shippingClone.setStartLocation(existingShipping.getStartLocation());
        shippingClone.setEndLocation(existingShipping.getEndLocation());
        shippingClone.setReceiverName(existingShipping.getReceiverName());
        shippingClone.setReceiverPhone(existingShipping.getReceiverPhone());
        shippingClone.setReceiverAddress(existingShipping.getReceiverAddress());
        shippingClone.setCity(existingShipping.getCity());
        shippingClone.setShippingFee(existingShipping.getShippingFee());
        shippingClone.setShippedAt(existingShipping.getShippedAt());
        shippingClone.setStatus(existingShipping.getStatus());
        // Persist clone
        Shipping savedShippingClone = shippingRepository.save(shippingClone);
        order.setShipping(savedShippingClone);
    order.setPayment(paymentEntity);
        order.setStatus(1); // Dang van chuyen
        order.setTotalAmount(0);

    Order savedOrder = orderRepository.save(order);

        // Create order details and calculate total
        long totalAmount = 0;
    List<OrderItem> orderItems = request.getItems().stream()
        .map(item -> {
                    Optional<Product> productOptional = productRepository.findById(item.getProductId());
                    if (productOptional.isEmpty()) {
                        throw new RuntimeException("Product not found with id: " + item.getProductId());
                    }

                    Product product = productOptional.get();

                    // Check stock
                    if (product.getStock() < item.getQuantity()) {
                        throw new RuntimeException("Insufficient stock for product: " + product.getProductName());
                    }

                    // Update stock
                    product.setStock(product.getStock() - item.getQuantity());
                    productRepository.save(product);

                    // Create order detail
                    OrderItem orderDetail = new OrderItem();
                    orderDetail.setOrder(savedOrder);
                    orderDetail.setProduct(product);
                    orderDetail.setQuantity(item.getQuantity());
                    orderDetail.setUnitPrice(product.getPrice());

                    return orderItemRepository.save(orderDetail);
                })
                .collect(Collectors.toList());

        // Calculate total amount
        totalAmount = orderItems.stream()
                .mapToLong(detail -> detail.getUnitPrice() * detail.getQuantity())
                .sum();

        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setOrderItems(orderItems);
        // Update payment amount with total
        if (paymentEntity != null) {
            paymentEntity.setAmount(java.math.BigDecimal.valueOf(totalAmount));
            paymentRepository.save(paymentEntity);
            savedOrder.setPayment(paymentEntity);
        }

        Order finalOrder = orderRepository.save(savedOrder);
        return OrderResponse.fromEntity(finalOrder);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        return OrderResponse.fromEntity(orderOptional.get());
    }

    @Override
    public OrderResponse updateOrder(String orderId, UpdateOrderRequest request) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();
        order.setStatus(request.getStatus());
        
        if (request.getPaymentId() != null) {
            Optional<Payment> paymentOptional = paymentRepository.findById(request.getPaymentId());
            paymentOptional.ifPresent(order::setPayment);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public void deleteOrder(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Restore product stock if order is not cancelled
            if (order.getStatus() != 0) {
                order.getOrderItems().forEach(detail -> {
                    Product product = detail.getProduct();
                    product.setStock(product.getStock() + detail.getQuantity());
                    productRepository.save(product);
                });
            }

            orderRepository.deleteById(orderId);
        }
    }

    @Override
    public List<OrderResponse> getOrdersByAccountId(String accountId) {
        List<Order> orders = orderRepository.findByAccountAccountId(accountId);
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAll(pageable);
        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    @Override
    public OrderResponse cancelOrder(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();

        // Only allow cancellation if order is not completed
        if (order.getStatus() == 2) {
            throw new RuntimeException("Cannot cancel completed order");
        }

        // Restore product stock
        order.getOrderItems().forEach(detail -> {
            Product product = detail.getProduct();
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(0); // Cancelled
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public OrderResponse completeOrder(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();

        // Only allow completion if order is not cancelled
        if (order.getStatus() == 0) {
            throw new RuntimeException("Cannot complete cancelled order");
        }

        order.setStatus(2); // Completed
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(int status, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByStatus(status, pageable);
        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    @Override
    public Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            Page<Order> ordersPage = orderRepository.findByCreatedAtBetween(start, end, pageable);
            List<OrderResponse> responses = ordersPage.getContent().stream()
                    .map(OrderResponse::fromEntity)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd");
        }
    }

    @Override
    public List<OrderItemResponse> getOrderDetailsByOrderId(String orderId) {
        // Validate order exists
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        List<OrderItem> orderDetails = orderItemRepository.findByOrderOrderId(orderId);
        return orderDetails.stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
