package fruitshop.payment_service.dto.response.Payment;

import lombok.Data;
import fruitshop.payment_service.entity.Payment;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentResponse {
    private String paymentId;
    private String paymentMethod;
    private int paymentStatus;
    private Date paymentDate;
    private BigDecimal amount;
    private String transactionId;
    private String orderId;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setTransactionId(payment.getTransactionId());
        response.setOrderId(payment.getOrderId());
        return response;
    }
}