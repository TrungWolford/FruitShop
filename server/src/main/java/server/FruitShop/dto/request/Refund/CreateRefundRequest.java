package server.FruitShop.dto.request.Refund;

import lombok.Data;
import java.util.List;

@Data
public class CreateRefundRequest {
    private String orderId;
    private String orderItemId;     // Add orderItemId for per-item refund
    private String reason;          // Lý do trả hàng / hoàn tiền
    private long refundAmount;      // Số tiền hoàn
    private String originalPaymentId;
    private List<String> imageUrls; // List of image URLs for evidence
}
