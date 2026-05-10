package fruitshop.order_service.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String paymentMethod;
    private int paymentStatus;
    private Date paymentDate;
    private BigDecimal amount;
    private String transactionId;
}
