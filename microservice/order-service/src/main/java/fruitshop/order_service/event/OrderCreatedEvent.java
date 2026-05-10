package fruitshop.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private String orderId;
    private String accountId;
    private long totalAmount;
    private int paymentMethod; // 0 = COD, 1 = MOMO
    private List<OrderItemDto> items;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private String productId;
        private int quantity;
        private long price;
    }
}
