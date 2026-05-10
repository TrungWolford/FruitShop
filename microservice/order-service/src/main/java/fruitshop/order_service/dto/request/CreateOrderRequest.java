package fruitshop.order_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import fruitshop.order_service.feign.dto.PaymentRequest;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String accountId;
    private String shippingId;
    private ShippingRequest shipping;
    private String paymentId;
    private PaymentRequest payment;
    private Integer paymentMethod;
    private List<OrderItemRequest> items;
    private long totalPrice;
    private int status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private String productId;
        private long unitPrice;
        private int quantity;
        private long totalPrice;
    }
}
