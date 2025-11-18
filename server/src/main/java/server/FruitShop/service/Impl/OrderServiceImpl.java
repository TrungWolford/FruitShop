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

        // NOTE: Shipping validation removed - shipping will be created when admin starts delivery
        // The shippingId in request is just a reference to address template, not used for order

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

        // Create order first without shipping
    Order order = new Order();
        order.setAccount(accountOptional.get());
    order.setPayment(paymentEntity);
        order.setStatus(1); // Chờ xác nhận (Customer vừa tạo đơn)
        order.setTotalAmount(0);
        order.setCreatedAt(new Date());

    Order savedOrder = orderRepository.save(order);

        // Create shipping with status "Chờ xác nhận" (1)
        Shipping shipping = null;
        Optional<Shipping> shippingTemplateOptional = shippingRepository.findById(request.getShippingId());
        if (shippingTemplateOptional.isPresent()) {
            Shipping template = shippingTemplateOptional.get();
            shipping = new Shipping();
            shipping.setAccount(accountOptional.get());
            shipping.setOrder(savedOrder);
            shipping.setReceiverName(template.getReceiverName());
            shipping.setReceiverPhone(template.getReceiverPhone());
            shipping.setReceiverAddress(template.getReceiverAddress());
            shipping.setCity(template.getCity());
            shipping.setShippingFee(template.getShippingFee());
            shipping.setStatus(1); // Chờ xác nhận
            shipping = shippingRepository.save(shipping);
            System.out.println("✅ Created shipping with status: 1 (Chờ xác nhận) for order: " + savedOrder.getOrderId());
        } else {
            System.err.println("⚠️ Shipping template not found with id: " + request.getShippingId());
        }
        
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

        // Calculate total amount from products
        totalAmount = orderItems.stream()
                .mapToLong(detail -> detail.getUnitPrice() * detail.getQuantity())
                .sum();

        // Add shipping fee to total amount
        if (shipping != null) {
            long shippingFee = shipping.getShippingFee();
            totalAmount += shippingFee;
            System.out.println("💰 Order total calculation:");
            System.out.println("   - Products subtotal: " + (totalAmount - shippingFee) + " VND");
            System.out.println("   - Shipping fee: " + shippingFee + " VND");
            System.out.println("   - Total amount: " + totalAmount + " VND");
        }

        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setOrderItems(orderItems);
        // Update payment amount with total (products + shipping)
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
        try {
            List<Order> orders = orderRepository.findByAccountAccountId(accountId);
            
            if (orders.isEmpty()) {
                System.out.println("ℹ️ No orders found for accountId: " + accountId);
                return List.of();
            }
            
            return orders.stream()
                    .map(order -> {
                        try {
                            return OrderResponse.fromEntity(order);
                        } catch (Exception e) {
                            System.err.println("❌ Error mapping order " + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error in getOrdersByAccountId: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        try {
            Page<Order> ordersPage = orderRepository.findAll(pageable);
            
            // Handle empty page
            if (ordersPage.isEmpty()) {
                System.out.println("ℹ️ No orders found in database");
                return Page.empty(pageable);
            }
            
            List<OrderResponse> responses = ordersPage.getContent().stream()
                    .map(order -> {
                        try {
                            return OrderResponse.fromEntity(order);
                        } catch (Exception e) {
                            System.err.println("❌ Error mapping order " + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("❌ Error in getAllOrders: " + e.getMessage());
            e.printStackTrace();
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, int newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public OrderResponse cancelOrder(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();

        // Only allow cancellation if order is pending (status = 1)
        if (order.getStatus() != 1) {
            throw new RuntimeException("Can only cancel orders with status 'Chờ xác nhận'");
        }

        // Restore product stock
        order.getOrderItems().forEach(detail -> {
            Product product = detail.getProduct();
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(0); // Đã hủy
        Order savedOrder = orderRepository.save(order);
        
        // Find and cancel shipping if exists
        Shipping orderShipping = shippingRepository.findByOrderOrderId(orderId);
        if (orderShipping != null) {
            orderShipping.setStatus(0); // Đã hủy
            shippingRepository.save(orderShipping);
        }
        
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public OrderResponse confirmOrder(String orderId) {
        System.out.println("🔍 Confirming order with ID: " + orderId);
        
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            System.err.println("❌ Order not found with id: " + orderId);
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();
        System.out.println("📦 Found order with status: " + order.getStatus());

        // Only allow confirmation if order is pending (status = 1)
        if (order.getStatus() != 1) {
            System.err.println("❌ Order status is " + order.getStatus() + ", expected 1 (Chờ xác nhận)");
            throw new RuntimeException("Can only confirm orders with status 'Chờ xác nhận'");
        }

        // Update order status to "Đã xác nhận"
        order.setStatus(2);
        Order savedOrder = orderRepository.save(order);
        System.out.println("✅ Order status updated to: " + savedOrder.getStatus());

        // Find and update shipping status to "Đã xác nhận" (2)
        System.out.println("🔍 Looking for shipping with orderId: " + orderId);
        Shipping orderShipping = shippingRepository.findByOrderOrderId(orderId);
        
        if (orderShipping != null) {
            System.out.println("📦 Found shipping with ID: " + orderShipping.getShippingId() + ", current status: " + orderShipping.getStatus());
            orderShipping.setStatus(2); // Đã xác nhận
            shippingRepository.save(orderShipping);
            System.out.println("✅ Shipping status updated to: 2 (Đã xác nhận)");
        } else {
            System.err.println("⚠️ Warning: Shipping not found for order: " + orderId);
            System.err.println("⚠️ Shipping should have been created during order creation");
            // Don't throw exception - allow order confirmation even if shipping missing
        }

        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public OrderResponse startDelivery(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();

        // Only allow delivery start if order is confirmed (status = 2)
        if (order.getStatus() != 2) {
            throw new RuntimeException("Can only start delivery for confirmed orders");
        }

        // Update order status to "Đang giao"
        order.setStatus(3);
        Order savedOrder = orderRepository.save(order);

        // Find and update existing shipping status to "Đang giao" (3)
        Shipping orderShipping = shippingRepository.findByOrderOrderId(orderId);
        
        if (orderShipping != null) {
            // Update shipping status to "Đang giao"
            orderShipping.setStatus(3); // Đang giao
            orderShipping.setShippedAt(new Date()); // Set thời gian bắt đầu giao
            shippingRepository.save(orderShipping);
            System.out.println("✅ Shipping status updated to: 3 (Đang giao)");
        } else {
            System.err.println("❌ Shipping not found for order: " + orderId);
            throw new RuntimeException("Shipping information not found for this order");
        }

        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public OrderResponse completeOrder(String orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        Order order = orderOptional.get();

        // Only allow completion if order is being delivered (status = 3)
        if (order.getStatus() != 3) {
            throw new RuntimeException("Can only complete orders that are being delivered");
        }

        // Update order status to "Giao thành công"
        order.setStatus(4);
        Order savedOrder = orderRepository.save(order);

        // Find and update shipping status to "Đã giao" (4)
        Shipping orderShipping = shippingRepository.findByOrderOrderId(orderId);
        
        if (orderShipping != null) {
            orderShipping.setStatus(4); // Đã giao
            shippingRepository.save(orderShipping);
            System.out.println("✅ Shipping status updated to: 4 (Đã giao)");
        }

        // Update payment status to "Hoàn thành" (1) when order is completed
        Payment orderPayment = order.getPayment();
        if (orderPayment != null) {
            // Only update if payment is still pending (0) or failed before (2)
            if (orderPayment.getPaymentStatus() == 0 || orderPayment.getPaymentStatus() == 2) {
                orderPayment.setPaymentStatus(1); // Completed
                orderPayment.setPaymentDate(new Date()); // Update payment date
                paymentRepository.save(orderPayment);
                System.out.println("✅ Payment status updated to: 1 (Hoàn thành) for payment: " + orderPayment.getPaymentId());
            } else {
                System.out.println("ℹ️ Payment already in final state: " + orderPayment.getPaymentStatus());
            }
        } else {
            System.out.println("⚠️ No payment found for order: " + orderId);
        }

        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(int status, Pageable pageable) {
        try {
            Page<Order> ordersPage = orderRepository.findByStatus(status, pageable);
            
            if (ordersPage.isEmpty()) {
                System.out.println("ℹ️ No orders found with status: " + status);
                return Page.empty(pageable);
            }
            
            List<OrderResponse> responses = ordersPage.getContent().stream()
                    .map(order -> {
                        try {
                            return OrderResponse.fromEntity(order);
                        } catch (Exception e) {
                            System.err.println("❌ Error mapping order " + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("❌ Error in getOrdersByStatus: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            Page<Order> ordersPage = orderRepository.findByCreatedAtBetween(start, end, pageable);
            
            if (ordersPage.isEmpty()) {
                System.out.println("ℹ️ No orders found between " + startDate + " and " + endDate);
                return Page.empty(pageable);
            }
            
            List<OrderResponse> responses = ordersPage.getContent().stream()
                    .map(order -> {
                        try {
                            return OrderResponse.fromEntity(order);
                        } catch (Exception e) {
                            System.err.println("❌ Error mapping order " + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
        } catch (ParseException e) {
            System.err.println("❌ Invalid date format: " + e.getMessage());
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd");
        } catch (Exception e) {
            System.err.println("❌ Error in getOrdersByDateRange: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
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

    @Override
    public Page<OrderResponse> searchOrders(String keyword, Pageable pageable) {
        try {
            // Search by orderId or accountName
            Page<Order> ordersPage = orderRepository.findByOrderIdContainingIgnoreCaseOrAccountAccountNameContainingIgnoreCase(
                    keyword, keyword, pageable);
            
            if (ordersPage.isEmpty()) {
                System.out.println("ℹ️ No orders found with keyword: " + keyword);
                return Page.empty(pageable);
            }
            
            List<OrderResponse> responses = ordersPage.getContent().stream()
                    .map(order -> {
                        try {
                            return OrderResponse.fromEntity(order);
                        } catch (Exception e) {
                            System.err.println("❌ Error mapping order " + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("❌ Error in searchOrders: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<OrderResponse> filterOrdersByStatus(int status, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByStatus(status, pageable);
        
        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    @Override
    public Page<OrderResponse> searchAndFilterOrders(String keyword, Integer status, Pageable pageable) {
        Page<Order> ordersPage;
        
        if (keyword != null && !keyword.trim().isEmpty() && status != null) {
            // Both search and filter
            ordersPage = orderRepository.findByOrderIdContainingIgnoreCaseAndStatusOrAccountAccountNameContainingIgnoreCaseAndStatus(
                    keyword, status, keyword, status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Only search
            ordersPage = orderRepository.findByOrderIdContainingIgnoreCaseOrAccountAccountNameContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else if (status != null) {
            // Only filter
            ordersPage = orderRepository.findByStatus(status, pageable);
        } else {
            // No filter or search
            ordersPage = orderRepository.findAll(pageable);
        }
        
        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    @Override
    public List<OrderItemResponse> getOrderItems(String orderId) {
        return getOrderDetailsByOrderId(orderId);
    }
}
