package server.FruitShop.dto.response.Refund;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import server.FruitShop.dto.response.Order.OrderResponse;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.Payment;
import server.FruitShop.entity.Refund;

import java.util.Date;

@Data
public class RefundResponse {
    private String refundId;

    private OrderResponse order;

    private String reason;          // Lý do trả hàng / hoàn tiền
    private String refundStatus;    // Pending, Approved, Rejected, Completed
    private Date requestedAt;       // Ngày người mua yêu cầu
    private Date processedAt;       // Ngày hoàn tất xử lý
    private long refundAmount;      // Số tiền hoàn

    private PaymentResponse originalPayment;

    public static RefundResponse fromEntity(Refund refund){
        RefundResponse response = new RefundResponse();
        response.setRefundId(refund.getRefundId());
        response.setReason(refund.getReason());
        response.setRefundStatus(refund.getRefundStatus());
        response.setRequestedAt(refund.getRequestedAt());
        response.setProcessedAt(refund.getProcessedAt());
        response.setRefundAmount(refund.getRefundAmount());
        
        // Map order
        if (refund.getOrder() != null) {
            response.setOrder(OrderResponse.fromEntity(refund.getOrder()));
        }
        
        // Map original payment
        if (refund.getOriginalPayment() != null) {
            response.setOriginalPayment(PaymentResponse.fromEntity(refund.getOriginalPayment()));
        }
        
        return response;
    }
}
