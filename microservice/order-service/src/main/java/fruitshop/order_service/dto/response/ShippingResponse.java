package fruitshop.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingResponse {
    private String shippingId;
    private String orderId;
    private String accountId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String city;
    private String shipperName;
    private long shippingFee;
    private Instant shippedAt;
    private int status;
}
