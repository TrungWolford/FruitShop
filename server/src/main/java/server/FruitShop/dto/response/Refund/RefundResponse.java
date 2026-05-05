package server.FruitShop.dto.response.Refund;

import lombok.Data;
import server.FruitShop.dto.response.Order.OrderResponse;
import server.FruitShop.dto.response.Order.OrderItemResponse;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.entity.Refund;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Data
public class RefundResponse {
    private String refundId;

    private OrderResponse order;
    
    private OrderItemResponse orderItem;  // Add orderItem for per-item refund

    private String reason;          // Lý do trả hàng / hoàn tiền
    private String refundStatus;    // Pending, Approved, Rejected, Completed
    private Date requestedAt;       // Ngày người mua yêu cầu
    private Date processedAt;       // Ngày hoàn tất xử lý
    private long refundAmount;      // Số tiền hoàn
    
    private List<String> imageUrls; // List of image URLs for evidence

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
        
        // Map orderItem
        if (refund.getOrderItem() != null) {
            response.setOrderItem(OrderItemResponse.fromEntity(refund.getOrderItem()));
        }
        
        // Parse imageUrls from JSON string
        if (refund.getImageUrls() != null && !refund.getImageUrls().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> urls = mapper.readValue(refund.getImageUrls(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                response.setImageUrls(urls);
            } catch (Exception e) {
                System.err.println("Error parsing imageUrls JSON: " + e.getMessage());
                response.setImageUrls(new ArrayList<>());
            }
        } else {
            response.setImageUrls(new ArrayList<>());
        }
        
        // Map original payment
        if (refund.getOriginalPayment() != null) {
            response.setOriginalPayment(PaymentResponse.fromEntity(refund.getOriginalPayment()));
        }
        
        return response;
    }
}

