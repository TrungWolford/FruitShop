package fruitshop.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundApprovedEvent {
    private String refundId;
    private String orderId;
    private long amount;
    private String approverName;
    private Date approvedAt;
}
