package server.FruitShop.dto.response.ChatBot;

import lombok.Data;
import server.FruitShop.entity.ChatSession;

import java.time.LocalDateTime;

/**
 * Response trả về thông tin 1 chat session (không kèm messages)
 * Dùng cho danh sách session (sidebar chat)
 */
@Data
public class ChatSessionResponse {

    private String sessionId;

    // Thông tin chủ session
    private String accountId;
    private String accountName;

    private String title;

    // Trạng thái: 0=Đóng, 1=Mở, 2=Chờ phản hồi
    private int status;

    // Preview tin nhắn cuối
    private String lastMessage;

    // Số tin chưa đọc
    private int unreadCount;

    // Intent cuối cùng trong session (để bot nhớ ngữ cảnh)
    private String lastIntent;

    // Trạng thái flow đặt hàng: null | "SELECTING" | "CONFIRMING" | "PAYING" | "DONE"
    private String orderFlowState;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------
    // Static factory: map từ entity → response (không kèm messages)
    // ---------------------------------------------------------------
    public static ChatSessionResponse fromEntity(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setSessionId(session.getSessionId());

        if (session.getAccount() != null) {
            response.setAccountId(session.getAccount().getAccountId());
            response.setAccountName(session.getAccount().getAccountName());
        }

        response.setTitle(session.getTitle());
        response.setStatus(session.getStatus());
        response.setLastMessage(session.getLastMessage());
        response.setUnreadCount(session.getUnreadCount());
        response.setLastIntent(session.getLastIntent());
        response.setOrderFlowState(session.getOrderFlowState());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());

        return response;
    }
}
