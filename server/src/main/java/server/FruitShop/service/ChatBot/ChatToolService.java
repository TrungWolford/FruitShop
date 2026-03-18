package server.FruitShop.service.ChatBot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Order.CreateOrderRequest;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Momo.CreateMomoResponse;
import server.FruitShop.dto.response.Order.OrderItemResponse;
import server.FruitShop.dto.response.Order.OrderResponse;
import server.FruitShop.dto.response.Refund.RefundResponse;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.OrderItem;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.Shipping;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.OrderRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.repository.ShippingRepository;
import server.FruitShop.service.OrderService;
import server.FruitShop.service.MomoService;
import server.FruitShop.service.Impl.RefundServiceImpl;
import server.FruitShop.service.Impl.CartServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Thực thi các "tools" (hàm) mà Gemini AI có thể gọi.
 * Mỗi tool nhận tham số JSON từ Gemini, query DB, và trả về JSON kết quả.
 */
@Service
public class ChatToolService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShippingRepository shippingRepository;
    private final AccountRepository accountRepository;
    private final OrderService orderService;
    private final MomoService momoService;
    private final RefundServiceImpl refundService;
    private final CartServiceImpl cartService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChatToolService(ProductRepository productRepository,
                           OrderRepository orderRepository,
                           ShippingRepository shippingRepository,
                           AccountRepository accountRepository,
                           OrderService orderService,
                           MomoService momoService,
                           RefundServiceImpl refundService,
                           CartServiceImpl cartService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.shippingRepository = shippingRepository;
        this.accountRepository = accountRepository;
        this.orderService = orderService;
        this.momoService = momoService;
        this.refundService = refundService;
        this.cartService = cartService;
    }

    // ================================================================
    // TOOL 1: Tìm sản phẩm theo tên
    // ================================================================

    /**
     * Tool: searchProducts
     * Gemini gọi khi user hỏi về sản phẩm cụ thể, tư vấn, hoặc muốn đặt hàng.
     *
     * @param keyword Tên sản phẩm cần tìm (VD: "táo", "xoài cát")
     * @param limit   Số lượng kết quả tối đa (mặc định 5)
     * @return JSON string: {"products": [...], "total": N}
     */
    public String searchProducts(String keyword, int limit) {
        try {
            int safeLimit = Math.min(Math.max(limit, 1), 10);
            List<Product> products = productRepository
                    .findByProductName(keyword != null ? keyword : "", PageRequest.of(0, safeLimit))
                    .getContent();

            List<Map<String, Object>> result = products.stream()
                    .map(this::productToMap)
                    .collect(Collectors.toList());

            return toJson(Map.of("products", result, "total", result.size(), "keyword", keyword));
        } catch (Exception e) {
            return errorJson("Không thể tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 2: Lấy chi tiết 1 sản phẩm
    // ================================================================

    /**
     * Tool: getProductDetail
     * Gemini gọi khi cần thông tin chi tiết về 1 sản phẩm (so sánh, tư vấn sâu).
     *
     * @param productId ID của sản phẩm
     * @return JSON string: {"productId": ..., "productName": ..., "price": ..., ...}
     */
    public String getProductDetail(String productId) {
        try {
            Product product = productRepository.findByIdWithCategories(productId);
            if (product == null) return errorJson("Không tìm thấy sản phẩm với ID: " + productId);

            // Load thêm images
            Product withImages = productRepository.findByIdWithImages(productId);

            Map<String, Object> detail = productToMap(product);
            if (withImages != null && withImages.getImages() != null) {
                List<String> imageUrls = withImages.getImages().stream()
                        .sorted(Comparator.comparingInt(img -> (img.getImageOrder() != null ? img.getImageOrder() : 0)))
                        .map(img -> img.getImageUrl())
                        .collect(Collectors.toList());
                detail.put("images", imageUrls);
            }
            if (product.getCategories() != null) {
                List<String> categories = product.getCategories().stream()
                        .map(c -> c.getCategoryName())
                        .collect(Collectors.toList());
                detail.put("categories", categories);
            }

            return toJson(detail);
        } catch (Exception e) {
            return errorJson("Không thể lấy chi tiết sản phẩm: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 3: Gợi ý sản phẩm theo ngân sách
    // ================================================================

    /**
     * Tool: suggestProducts
     * Gemini gọi khi user nhờ gợi ý sản phẩm phù hợp.
     *
     * @param maxPrice Ngân sách tối đa (VND), -1 nếu không giới hạn
     * @param limit    Số lượng gợi ý tối đa
     * @return JSON string: {"products": [...]}
     */
    public String suggestProducts(long maxPrice, int limit) {
        try {
            int safeLimit = Math.min(Math.max(limit, 1), 8);
            long safeMax = maxPrice > 0 ? maxPrice : Long.MAX_VALUE;

            List<Product> products = productRepository
                    .findProductsByCategoryStatusAndInRangePrice(1, 0, safeMax, PageRequest.of(0, safeLimit))
                    .getContent();

            List<Map<String, Object>> result = products.stream()
                    .map(this::productToMap)
                    .collect(Collectors.toList());

            return toJson(Map.of("products", result, "total", result.size(), "maxPrice", safeMax));
        } catch (Exception e) {
            return errorJson("Không thể gợi ý sản phẩm: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 4: Lấy danh sách đơn hàng của user
    // ================================================================

    /**
     * Tool: getOrdersByAccount
     * Gemini gọi khi user hỏi về đơn hàng của mình.
     *
     * @param accountId ID tài khoản của user
     * @return JSON string: {"orders": [...]}
     */
    public String getOrdersByAccount(String accountId) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần cung cấp accountId để tra cứu đơn hàng.");
            }

            List<Order> orders = orderRepository.findByAccountAccountId(accountId);
            if (orders.isEmpty()) {
                return toJson(Map.of("orders", List.of(), "message", "Bạn chưa có đơn hàng nào."));
            }

            // 3 đơn gần nhất
            List<Map<String, Object>> result = orders.stream()
                    .filter(o -> o.getCreatedAt() != null)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(3)
                    .map(this::orderToMap)
                    .collect(Collectors.toList());

            return toJson(Map.of("orders", result, "total", orders.size()));
        } catch (Exception e) {
            return errorJson("Không thể tra cứu đơn hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 5: Lấy chi tiết 1 đơn hàng
    // ================================================================

    /**
     * Tool: getOrderDetail
     * Gemini gọi khi user hỏi chi tiết về 1 đơn hàng cụ thể.
     *
     * @param orderId Mã đơn hàng
     * @return JSON string với đầy đủ thông tin đơn hàng + items
     */
    public String getOrderDetail(String orderId) {
        try {
            Optional<Order> opt = orderRepository.findById(orderId);
            if (opt.isEmpty()) return errorJson("Không tìm thấy đơn hàng: " + orderId);

            Order order = opt.get(); 
            Map<String, Object> detail = orderToMap(order);

            if (order.getOrderItems() != null) {
                List<Map<String, Object>> items = order.getOrderItems().stream()
                        .map(item -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("orderDetailId", item.getOrderDetailId());
                            m.put("productName", item.getProduct() != null ? item.getProduct().getProductName() : "");
                            m.put("quantity", item.getQuantity());
                            m.put("unitPrice", item.getUnitPrice());
                            m.put("subtotal", (long) item.getQuantity() * item.getUnitPrice());
                            return m;
                        })
                        .collect(Collectors.toList());
                detail.put("items", items);
            }

            if (order.getShipping() != null) {
                Map<String, Object> shipping = new LinkedHashMap<>();
                shipping.put("address", order.getShipping().getReceiverAddress());
                shipping.put("receiverName", order.getShipping().getReceiverName());
                shipping.put("phone", order.getShipping().getReceiverPhone());
                detail.put("shipping", shipping);
            }

            return toJson(detail);
        } catch (Exception e) {
            return errorJson("Không thể lấy chi tiết đơn hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 6: Lấy danh sách địa chỉ giao hàng của user
    // ================================================================

    /**
     * Tool: getUserShippingAddresses
     * Gemini gọi để lấy danh sách địa chỉ đã lưu của user trước khi đặt hàng.
     *
     * @param accountId ID tài khoản của user
     * @return JSON string: {"addresses": [...], "total": N}
     */
    public String getUserShippingAddresses(String accountId) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để xem địa chỉ giao hàng.");
            }

            // Chỉ lấy địa chỉ mẫu (không gắn với đơn hàng nào)
            List<Shipping> addresses = shippingRepository.findByAccountAccountId(accountId)
                    .stream()
                    .filter(s -> s.getOrder() == null)
                    .collect(Collectors.toList());

            if (addresses.isEmpty()) {
                return toJson(Map.of(
                        "addresses", List.of(),
                        "total", 0,
                        "message", "Bạn chưa có địa chỉ giao hàng nào. Vui lòng thêm địa chỉ trên trang web."
                ));
            }

            List<Map<String, Object>> result = addresses.stream()
                    .map(s -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("shippingId", s.getShippingId());
                        m.put("receiverName", s.getReceiverName());
                        m.put("receiverPhone", s.getReceiverPhone());
                        m.put("receiverAddress", s.getReceiverAddress());
                        m.put("city", s.getCity());
                        m.put("shippingFee", s.getShippingFee());
                        return m;
                    })
                    .collect(Collectors.toList());

            return toJson(Map.of("addresses", result, "total", result.size()));
        } catch (Exception e) {
            return errorJson("Không thể lấy địa chỉ giao hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 7: Tạo địa chỉ giao hàng mới từ thông tin user cung cấp
    // ================================================================

    /**
     * Tool: createShippingAddress
     * Gemini gọi sau khi thu thập đủ thông tin địa chỉ từ user.
     * Tạo bản ghi shipping template (không gắn với đơn hàng nào) và trả về shippingId
     * để dùng cho bước đặt hàng tiếp theo.
     *
     * @param accountId       ID tài khoản user
     * @param receiverName    Tên người nhận
     * @param receiverPhone   Số điện thoại người nhận
     * @param receiverAddress Địa chỉ cụ thể (số nhà, đường, phường/xã)
     * @param city            Tỉnh/Thành phố
     * @return JSON string: {"shippingId": "...", "receiverName": ..., "shippingFee": ...}
     */
    public String createShippingAddress(String accountId, String receiverName,
                                        String receiverPhone, String receiverAddress,
                                        String city) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để lưu địa chỉ giao hàng.");
            }

            server.FruitShop.entity.Account account = accountRepository.findById(accountId)
                    .orElse(null);
            if (account == null) {
                return errorJson("Không tìm thấy tài khoản: " + accountId);
            }

            // Phí ship mặc định, admin có thể cập nhật sau
            long defaultShippingFee = 30_000L;

            Shipping shipping = new Shipping();
            shipping.setAccount(account);
            shipping.setReceiverName(receiverName);
            shipping.setReceiverPhone(receiverPhone);
            shipping.setReceiverAddress(receiverAddress);
            shipping.setCity(city);
            shipping.setShippingFee(defaultShippingFee);
            shipping.setStatus(1); // Sẵn sàng dùng

            Shipping saved = shippingRepository.save(shipping);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("shippingId", saved.getShippingId());
            result.put("receiverName", saved.getReceiverName());
            result.put("receiverPhone", saved.getReceiverPhone());
            result.put("receiverAddress", saved.getReceiverAddress());
            result.put("city", saved.getCity());
            result.put("shippingFee", saved.getShippingFee());
            result.put("message", "Đã lưu địa chỉ giao hàng thành công.");

            return toJson(result);
        } catch (Exception e) {
            return errorJson("Không thể lưu địa chỉ giao hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 8: Tạo đơn hàng từ chat
    // ================================================================

    /**
     * Tool: createOrderFromChat
     * Gemini gọi sau khi user xác nhận muốn đặt hàng.
     * Trả về hóa đơn chi tiết để user kiểm tra lần cuối.
     *
     * @param accountId     ID tài khoản của user
     * @param itemsJson     JSON string danh sách sản phẩm, VD: [{"productId":"xxx","quantity":2}]
     * @param shippingId    ID địa chỉ giao hàng đã chọn
     * @param paymentMethod Phương thức thanh toán: 0=COD, 1=Chuyển khoản
     * @return JSON string hóa đơn chi tiết
     */
    public String createOrderFromChat(String accountId, String itemsJson,
                                      String shippingId, int paymentMethod) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để đặt hàng.");
            }
            // Nếu shippingId rỗng (Gemini gọi createOrderFromChat cùng round với createShippingAddress),
            // fallback: lấy template mới nhất của account (order == null)
            if (shippingId == null || shippingId.isBlank()) {
                List<Shipping> templates = shippingRepository.findByAccountAccountId(accountId)
                        .stream()
                        .filter(s -> s.getOrder() == null)
                        .collect(Collectors.toList());
                if (templates.size() == 1) {
                    shippingId = templates.get(0).getShippingId();
                } else if (!templates.isEmpty()) {
                    // Nhiều template → lấy cái cuối trong list (thường là mới nhất)
                    shippingId = templates.get(templates.size() - 1).getShippingId();
                } else {
                    return errorJson("Cần cung cấp địa chỉ giao hàng.");
                }
            }

            // Nếu itemsJson rỗng → tự động đọc từ DB cart
            List<Map<String, Object>> rawItems;
            if (itemsJson == null || itemsJson.isBlank() || itemsJson.equals("[]")) {
                List<CartItemResponse> cartItems = cartService.getCartItemsByAccountId(accountId);
                if (cartItems.isEmpty()) {
                    return errorJson("Giỏ hàng đang trống. Vui lòng thêm sản phẩm trước khi đặt hàng.");
                }
                rawItems = cartItems.stream()
                        .map(ci -> {
                            Map<String, Object> m = new java.util.HashMap<>();
                            m.put("productId", ci.getProductId());
                            m.put("quantity", ci.getQuantity());
                            return m;
                        })
                        .collect(Collectors.toList());
            } else {
                rawItems = objectMapper.readValue(itemsJson, new TypeReference<>() {});
            }

            List<CreateOrderRequest.OrderItemRequest> orderItems = rawItems.stream()
                    .map(item -> {
                        CreateOrderRequest.OrderItemRequest oir = new CreateOrderRequest.OrderItemRequest();
                        oir.setProductId(String.valueOf(item.get("productId")));
                        Object qty = item.get("quantity");
                        oir.setQuantity(qty instanceof Number ? ((Number) qty).intValue() : 1);
                        return oir;
                    })
                    .collect(Collectors.toList());

            CreateOrderRequest request = new CreateOrderRequest();
            request.setAccountId(accountId);
            request.setShippingId(shippingId);
            request.setPaymentMethod(paymentMethod);
            request.setStatus(1);
            request.setItems(orderItems);

            OrderResponse order = orderService.createOrder(request);

            // Sau khi đơn hàng được tạo, OrderServiceImpl đã copy shipping template
            // thành 1 bản mới gắn với order. Xóa bản template trung gian nếu nó
            // không có order (tức là vừa được tạo bởi createShippingAddress từ chat).
            shippingRepository.findById(shippingId).ifPresent(template -> {
                if (template.getOrder() == null) {
                    shippingRepository.delete(template);
                }
            });

            // Xóa giỏ hàng sau khi đặt hàng thành công
            try {
                cartService.clearCart(accountId);
            } catch (Exception e) {
                System.err.println("⚠️ Could not clear cart after order: " + e.getMessage());
            }

            return toJson(buildInvoiceMap(order));
        } catch (Exception e) {
            return errorJson("Không thể tạo đơn hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 9: Thêm sản phẩm vào giỏ hàng
    // ================================================================

    /**
     * Tool: addToCart
     * Gemini gọi sau khi user xác nhận sản phẩm và số lượng muốn mua.
     * Ghi ngay vào DB cart → sản phẩm từ chat và website dùng chung 1 giỏ.
     *
     * @param accountId ID tài khoản user
     * @param productId ID sản phẩm cần thêm
     * @param quantity  Số lượng
     * @return JSON string: {"cartItemId": "...", "productName": "...", "quantity": N, ...}
     */
    public String addToCart(String accountId, String productId, int quantity) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để thêm vào giỏ hàng.");
            }
            if (productId == null || productId.isBlank()) {
                return errorJson("Cần cung cấp productId.");
            }
            if (quantity <= 0) quantity = 1;

            CreateCartItemRequest request = new CreateCartItemRequest();
            request.setProductId(productId);
            request.setQuantity(quantity);

            CartItemResponse item = cartService.addCartItem(accountId, request);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("cartItemId", item.getCartItemId());
            result.put("productId", item.getProductId());
            result.put("productName", item.getProductName());
            result.put("productPrice", item.getProductPrice());
            result.put("quantity", item.getQuantity());
            result.put("totalPrice", item.getTotalPrice());
            result.put("message", "Đã thêm \"" + item.getProductName() + "\" x" + item.getQuantity() + " vào giỏ hàng.");
            return toJson(result);
        } catch (Exception e) {
            return errorJson("Không thể thêm vào giỏ hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 10: Xem giỏ hàng hiện tại
    // ================================================================

    /**
     * Tool: getCartItems
     * Gemini gọi để xem toàn bộ sản phẩm đang có trong giỏ hàng của user.
     * Bao gồm cả sản phẩm được thêm qua chat lẫn qua website.
     *
     * @param accountId ID tài khoản user
     * @return JSON string: {"items": [...], "total": N, "subtotal": N}
     */
    public String getCartItems(String accountId) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để xem giỏ hàng.");
            }

            List<CartItemResponse> cartItems = cartService.getCartItemsByAccountId(accountId);
            if (cartItems.isEmpty()) {
                return toJson(Map.of("items", List.of(), "total", 0, "subtotal", 0,
                        "message", "Giỏ hàng đang trống."));
            }

            List<Map<String, Object>> items = cartItems.stream()
                    .map(ci -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("cartItemId", ci.getCartItemId());
                        m.put("productId", ci.getProductId());
                        m.put("productName", ci.getProductName());
                        m.put("productPrice", ci.getProductPrice());
                        m.put("quantity", ci.getQuantity());
                        m.put("totalPrice", ci.getTotalPrice());
                        return m;
                    })
                    .collect(Collectors.toList());

            long subtotal = cartItems.stream().mapToLong(CartItemResponse::getTotalPrice).sum();
            return toJson(Map.of("items", items, "total", items.size(), "subtotal", subtotal));
        } catch (Exception e) {
            return errorJson("Không thể lấy giỏ hàng: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 11: Xóa sản phẩm khỏi giỏ hàng
    // ================================================================

    /**
     * Tool: removeFromCart
     * Gemini gọi khi user muốn bỏ 1 sản phẩm ra khỏi giỏ.
     *
     * @param cartItemId ID của CartItem cần xóa
     * @return JSON string xác nhận đã xóa
     */
    public String removeFromCart(String cartItemId) {
        try {
            if (cartItemId == null || cartItemId.isBlank()) {
                return errorJson("Cần cung cấp cartItemId để xóa.");
            }
            cartService.removeCartItem(cartItemId);
            return toJson(Map.of("success", true, "message", "Đã xóa sản phẩm khỏi giỏ hàng."));
        } catch (Exception e) {
            return errorJson("Không thể xóa sản phẩm: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 12: Tạo thanh toán MoMo cho đơn hàng
    // ================================================================

    /**
     * Tool: createMomoPayment
     * Gemini gọi SAU KHI createOrderFromChat thành công với paymentMethod=1.
     * Trả về payUrl và qrCodeUrl để hiển thị cho user thanh toán.
     *
     * @param orderId Mã đơn hàng vừa tạo
     * @return JSON string: {"payUrl": "...", "qrCodeUrl": "...", "deeplink": "..."}
     */
    public String createMomoPayment(String orderId) {
        try {
            if (orderId == null || orderId.isBlank()) {
                return errorJson("Cần cung cấp orderId để tạo thanh toán MoMo.");
            }

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return errorJson("Không tìm thấy đơn hàng: " + orderId);
            }

            Order order = orderOpt.get();
            if (order.getStatus() == 0) {
                return errorJson("Đơn hàng đã bị hủy, không thể tạo thanh toán.");
            }

            long amount = order.getTotalAmount();
            String orderInfo = "Thanh toán đơn hàng #" + orderId;

            CreateMomoResponse response = momoService.createQR(orderId, amount, orderInfo);

            if (response.isSuccess()) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("success", true);
                result.put("orderId", orderId);
                result.put("amount", amount);
                result.put("payUrl", response.getPayUrl());
                result.put("qrCodeUrl", response.getQrCodeUrl());
                result.put("deeplink", response.getDeeplink());
                result.put("message", "Tạo thanh toán MoMo thành công. Vui lòng quét QR hoặc nhấn link để thanh toán.");
                return toJson(result);
            } else {
                return errorJson("Tạo thanh toán MoMo thất bại: " + response.getErrorMessage());
            }
        } catch (Exception e) {
            return errorJson("Không thể tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 10: Lấy danh sách yêu cầu hoàn trả theo đơn hàng
    // ================================================================

    /**
     * Tool: getRefundsByOrder
     * Gemini gọi để kiểm tra đơn hàng đã có yêu cầu hoàn trả chưa.
     *
     * @param orderId Mã đơn hàng cần kiểm tra
     * @return JSON string: {"refunds": [...], "total": N}
     */
    public String getRefundsByOrder(String orderId) {
        try {
            if (orderId == null || orderId.isBlank()) {
                return errorJson("Cần cung cấp orderId để tra cứu hoàn trả.");
            }

            List<RefundResponse> refunds = refundService.getRefundsByOrderId(orderId);

            List<Map<String, Object>> result = refunds.stream()
                    .map(r -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("refundId", r.getRefundId());
                        m.put("reason", r.getReason());
                        m.put("refundAmount", r.getRefundAmount());
                        m.put("refundStatus", r.getRefundStatus());
                        m.put("requestedAt", r.getRequestedAt() != null ? r.getRequestedAt().toString() : null);
                        return m;
                    })
                    .collect(Collectors.toList());

            return toJson(Map.of("refunds", result, "total", result.size()));
        } catch (Exception e) {
            return errorJson("Không thể tra cứu yêu cầu hoàn trả: " + e.getMessage());
        }
    }

    // ================================================================
    // TOOL 11: Tạo yêu cầu hoàn trả từ chat
    // ================================================================

    /**
     * Tool: createRefundFromChat
     * Gemini gọi sau khi thu thập đủ thông tin để tạo yêu cầu hoàn trả.
     *
     * @param accountId    ID tài khoản user (để xác minh đơn thuộc về user)
     * @param orderId      Mã đơn hàng cần hoàn trả
     * @param reason       Lý do hoàn trả
     * @param refundAmount Số tiền hoàn trả (0 = hoàn toàn bộ)
     * @return JSON string kết quả yêu cầu hoàn trả
     */
    public String createRefundFromChat(String accountId, String orderId,
                                       String reason, long refundAmount) {
        try {
            if (accountId == null || accountId.isBlank()) {
                return errorJson("Cần đăng nhập để yêu cầu hoàn trả.");
            }
            if (orderId == null || orderId.isBlank()) {
                return errorJson("Cần cung cấp mã đơn hàng để yêu cầu hoàn trả.");
            }
            if (reason == null || reason.isBlank()) {
                return errorJson("Cần cung cấp lý do hoàn trả.");
            }

            // Kiểm tra đơn hàng tồn tại và thuộc về user
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return errorJson("Không tìm thấy đơn hàng: " + orderId);
            }
            Order order = orderOpt.get();
            if (order.getAccount() == null || !accountId.equals(order.getAccount().getAccountId())) {
                return errorJson("Đơn hàng không thuộc về tài khoản của bạn.");
            }

            // Nếu refundAmount = 0 thì hoàn toàn bộ giá trị đơn hàng
            long amount = refundAmount > 0 ? refundAmount : order.getTotalAmount();

            CreateRefundRequest request = new CreateRefundRequest();
            request.setOrderId(orderId);
            request.setReason(reason);
            request.setRefundAmount(amount);

            RefundResponse refund = refundService.createRefund(request);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("refundId", refund.getRefundId());
            result.put("orderId", orderId);
            result.put("reason", refund.getReason());
            result.put("refundAmount", refund.getRefundAmount());
            result.put("refundStatus", refund.getRefundStatus());
            result.put("requestedAt", refund.getRequestedAt() != null ? refund.getRequestedAt().toString() : null);
            result.put("message", "Yêu cầu hoàn trả đã được gửi thành công. Shop sẽ xem xét và phản hồi trong thời gian sớm nhất.");
            return toJson(result);
        } catch (Exception e) {
            return errorJson("Không thể tạo yêu cầu hoàn trả: " + e.getMessage());
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /** Chuyển OrderResponse thành map hóa đơn gọn cho AI trả lời */
    private Map<String, Object> buildInvoiceMap(OrderResponse order) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", order.getOrderId());
        m.put("status", order.getStatus());
        m.put("statusLabel", "Chờ xác nhận");
        m.put("totalAmount", order.getTotalAmount());
        m.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);

        if (order.getOrderItems() != null) {
            List<Map<String, Object>> items = order.getOrderItems().stream()
                    .map(item -> {
                        Map<String, Object> i = new LinkedHashMap<>();
                        i.put("productName", item.getProductName());
                        i.put("quantity", item.getQuantity());
                        i.put("unitPrice", item.getUnitPrice());
                        i.put("subtotal", item.getTotalPrice());
                        return i;
                    })
                    .collect(Collectors.toList());
            m.put("items", items);
        }

        if (order.getShipping() != null) {
            Map<String, Object> shipping = new LinkedHashMap<>();
            shipping.put("receiverName", order.getShipping().getReceiverName());
            shipping.put("receiverPhone", order.getShipping().getReceiverPhone());
            shipping.put("address", order.getShipping().getReceiverAddress());
            shipping.put("city", order.getShipping().getCity());
            shipping.put("shippingFee", order.getShipping().getShippingFee());
            m.put("shipping", shipping);
        }

        if (order.getPayment() != null) {
            m.put("paymentMethod", order.getPayment().getPaymentMethod());
        }

        return m;
    }



    private Map<String, Object> productToMap(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", p.getProductId());
        m.put("productName", p.getProductName());
        m.put("price", p.getPrice());
        m.put("stock", p.getStock());
        m.put("status", p.getStatus());

        // Lấy ảnh đại diện (isMain = true hoặc imageOrder = 0)
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            p.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                    .findFirst()
                    .or(() -> p.getImages().stream().min(
                            Comparator.comparingInt(img -> img.getImageOrder() != null ? img.getImageOrder() : 999)))
                    .ifPresent(img -> m.put("imageUrl", img.getImageUrl()));
        }
        return m;
    }

    private Map<String, Object> orderToMap(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", o.getOrderId());
        m.put("status", o.getStatus());
        m.put("statusLabel", resolveOrderStatus(o.getStatus()));
        m.put("totalAmount", o.getTotalAmount());
        m.put("itemCount", o.getOrderItems() != null ? o.getOrderItems().size() : 0);
        m.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
        return m;
    }

    private String resolveOrderStatus(int status) {
        return switch (status) {
            case 1 -> "Chờ xác nhận";
            case 2 -> "Đã xác nhận";
            case 3 -> "Đang giao";
            case 4 -> "Giao thành công";
            case 0 -> "Đã hủy";
            default -> "Không xác định";
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"JSON serialization failed\"}";
        }
    }

    private String errorJson(String message) {
        return "{\"error\":\"" + message.replace("\"", "'") + "\"}";
    }
}
