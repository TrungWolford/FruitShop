package fruitshop.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String accountId;
    private long totalAmount;
    // Để an toàn khi truyền qua Message Broker, ta chỉ truyền các field cơ bản của Item
    private List<OrderItemDto> items;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private int quantity;
        private long price;
    }
}
