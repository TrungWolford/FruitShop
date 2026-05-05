package fruitshop.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundFailedEvent {
    private String refundId;
    private String orderId;
    private String reason;
}
