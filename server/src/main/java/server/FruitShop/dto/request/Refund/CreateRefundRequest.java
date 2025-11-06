package server.FruitShop.dto.request.Refund;

import lombok.Data;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.Payment;

import java.util.Date;

@Data
public class CreateRefundRequest {
    private String orderId;
    private String reason;          // Lý do trả hàng / hoàn tiền
    private long refundAmount;      // Số tiền hoàn
    private String originalPaymentId;
}
