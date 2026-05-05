package server.FruitShop.dto.response.ChatBot;

import lombok.Data;
import server.FruitShop.entity.ChatMessage;

import java.time.LocalDateTime;

/**
 * Response trả về thông tin 1 tin nhắn
 */
@Data
public class ChatResponse {

    private String messageId;
    private String sessionId;

    // Thông tin người gửi
    private String senderId;
    private String senderName;
    private String senderRole;   // "CUSTOMER" | "ADMIN" | "SYSTEM"

    // Nội dung
    private String content;
    private String messageType;  // "TEXT" | "IMAGE" | "FILE"

    // Trạng thái: 0=Sent, 1=Delivered, 2=Read
    private int status;

    // Đã bị thu hồi chưa
    private boolean deleted;

    // Intent bot nhận diện được từ tin nhắn
    private String intent;

    // Metadata JSON — chứa data đính kèm theo intent:
    // VD tư vấn sản phẩm:  {"products": [{productId, name, price, imageUrl}]}
    // VD so sánh sản phẩm: {"compareTable": [{field, product1Value, product2Value}]}
    // VD tra cứu đơn hàng: {"order": {orderId, status, totalAmount, items}}
    // VD gợi ý sản phẩm:   {"suggestions": [{productId, name, price, reason}]}
    // VD đặt hàng:         {"orderFlowState": "CONFIRMING", "cart": {...}}
    private String metadata;

    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Static factory: map từ entity → response
    // ---------------------------------------------------------------
    public static ChatResponse fromEntity(ChatMessage message) {
        ChatResponse response = new ChatResponse();
        response.setMessageId(message.getMessageId());
        response.setSessionId(message.getChatSession().getSessionId());

        if (message.getSender() != null) {
            response.setSenderId(message.getSender().getAccountId());
            response.setSenderName(message.getSender().getAccountName());
        }

        response.setSenderRole(message.getSenderRole());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setStatus(message.getStatus());
        response.setDeleted(message.isDeleted());
        response.setIntent(message.getIntent());
        response.setMetadata(message.getMetadata());
        response.setCreatedAt(message.getCreatedAt());

        return response;
    }
}
