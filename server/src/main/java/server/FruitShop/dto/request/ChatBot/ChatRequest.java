package server.FruitShop.dto.request.ChatBot;

import lombok.Data;

/**
 * Request gửi 1 tin nhắn trong 1 session đã có (hoặc tạo session mới)
 */
@Data
public class ChatRequest {

    // ID của session (null nếu muốn tạo session mới)
    private String sessionId;

    // ID của người gửi (null nếu là guest chưa đăng nhập)
    private String senderId;

    // Nội dung tin nhắn
    private String content;

    // Loại tin nhắn: "TEXT" | "IMAGE" | "FILE"
    private String messageType = "TEXT";

    // Vai trò người gửi: "CUSTOMER" | "ADMIN" | "SYSTEM"
    private String senderRole;

    // Intent gợi ý từ frontend (optional) — bot sẽ tự detect nếu null:
    // "PRODUCT_ADVICE"  | "PRODUCT_COMPARE" | "ORDER_LOOKUP"
    // "PRODUCT_SUGGEST" | "ORDER_PLACE"     | "PAYMENT" | "GENERAL"
    private String intent;

    // Metadata context cho intent (optional, JSON string):
    // VD so sánh sản phẩm: {"productIds": ["id1", "id2"]}
    // VD tra cứu đơn hàng: {"orderId": "ORD-001"}
    // VD đặt hàng:         {"productId": "p-1", "quantity": 2}
    private String metadata;
}
