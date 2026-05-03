package fruitshop.payment_service.dto.request.Payment;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentRequest {
    private String orderId;
    private String paymentMethod;
    private int paymentStatus;
    private Date paymentDate;
    private BigDecimal amount;
    private String transactionId;
}