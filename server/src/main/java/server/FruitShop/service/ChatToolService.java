package server.FruitShop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.Product;
import server.FruitShop.repository.OrderRepository;
import server.FruitShop.repository.ProductRepository;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChatToolService(ProductRepository productRepository,
                           OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
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
    // HELPERS
    // ================================================================

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
