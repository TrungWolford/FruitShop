package server.FruitShop.dto.response.Payment;

import lombok.Data;
import server.FruitShop.entity.Payment;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentResponse {
    private String paymentId;
    private String paymentMethod;
    private int paymentStatus; // 0: Pending, 1: Completed, 2: Failed, 3: Refunded
    private Date paymentDate;
    private BigDecimal amount;
    private String transactionId;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setTransactionId(payment.getTransactionId());
        return response;
    }
}
