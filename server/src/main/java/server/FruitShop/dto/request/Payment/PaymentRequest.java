package server.FruitShop.dto.request.Payment;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentRequest {
    private String paymentMethod; // COD, Bank Transfer, E-Wallet, Credit Card, etc.
    private int paymentStatus; // 0: Pending, 1: Completed, 2: Failed, 3: Refunded
    private Date paymentDate;
    private BigDecimal amount;
    private String transactionId;
}
