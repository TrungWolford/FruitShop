package server.FruitShop.dto.request.Order;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String accountId;
    private String shippingId;
    private String paymentId;
    private List<OrderItemRequest> items;
    private long totalPrice;
    private int status;

    @Data
    public static class OrderItemRequest {
        private String productId;
        private long unitPrice;
        private int quantity;
        private long totalPrice;
    }
}
