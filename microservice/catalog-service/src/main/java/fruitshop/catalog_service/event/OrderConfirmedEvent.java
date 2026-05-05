package fruitshop.catalog_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    private String orderId;
    private String accountId;
    private List<OrderItemDto> items;
    private Instant confirmedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private int quantity;
    }
}
