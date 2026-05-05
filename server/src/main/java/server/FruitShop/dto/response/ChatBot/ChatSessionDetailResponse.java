package server.FruitShop.dto.response.ChatBot;

import lombok.Data;
import server.FruitShop.entity.ChatSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response trả về 1 chat session kèm toàn bộ messages
 * Dùng khi mở một cuộc hội thoại cụ thể (chat window)
 */
@Data
public class ChatSessionDetailResponse {

    private String sessionId;

    // Thông tin chủ session
    private String accountId;
    private String accountName;

    private String title;

    // Trạng thái: 0=Đóng, 1=Mở, 2=Chờ phản hồi
    private int status;

    private String lastMessage;
    private int unreadCount;

    // Intent cuối cùng trong session (để bot nhớ ngữ cảnh)
    private String lastIntent;

    // Trạng thái flow đặt hàng: null | "SELECTING" | "CONFIRMING" | "PAYING" | "DONE"
    private String orderFlowState;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Toàn bộ tin nhắn trong session (sắp xếp theo thời gian)
    private List<ChatResponse> messages;

    // ---------------------------------------------------------------
    // Static factory: map từ entity → response (kèm messages)
    // ---------------------------------------------------------------
    public static ChatSessionDetailResponse fromEntity(ChatSession session) {
        ChatSessionDetailResponse response = new ChatSessionDetailResponse();
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

        // Map danh sách messages
        if (session.getMessages() != null) {
            response.setMessages(
                session.getMessages().stream()
                    .map(ChatResponse::fromEntity)
                    .collect(Collectors.toList())
            );
        }

        return response;
    }
}
