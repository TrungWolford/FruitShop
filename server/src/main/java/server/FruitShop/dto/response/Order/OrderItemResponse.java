package server.FruitShop.dto.response.Order;

import lombok.Data;
import server.FruitShop.entity.OrderItem;
import server.FruitShop.entity.ProductImage;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderItemResponse {
    private String orderDetailId;
    private String productId;
    private String productName;
    private int quantity;
    private long unitPrice;
    private long totalPrice;
    private List<String> productImages;
    private String status; // Status của orderItem (null, "returned", "returning", etc.)

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setOrderDetailId(orderItem.getOrderDetailId());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getUnitPrice() * orderItem.getQuantity());
        response.setStatus(orderItem.getStatus()); // Map status field

        if (orderItem.getProduct() != null) {
            response.setProductId(orderItem.getProduct().getProductId());
            response.setProductName(orderItem.getProduct().getProductName());

            // Trả về danh sách ảnh (ưu tiên ảnh chính lên đầu nếu có)
            if (orderItem.getProduct().getImages() != null) {
                List<String> images = orderItem.getProduct().getImages().stream()
                        .sorted((a, b) -> Boolean.compare(!Boolean.TRUE.equals(b.getIsMain()), !Boolean.TRUE.equals(a.getIsMain())))
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList());
                response.setProductImages(images);
            }
        }

        return response;
    }
}

