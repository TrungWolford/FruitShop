package fruitshop.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import fruitshop.order_service.feign.dto.PaymentResponse;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private String accountId;
    private String accountName;
    private Instant createdAt;
    private int status;
    private long totalAmount;
    private List<OrderItemResponse> orderItems;
    private int totalItems;
    private ShippingResponse shipping;
    private PaymentResponse payment;
}
