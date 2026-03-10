package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.ChatBot.ChatRequest;
import server.FruitShop.dto.request.ChatBot.CreateSessionRequest;
import server.FruitShop.dto.request.ChatBot.UpdateSessionRequest;
import server.FruitShop.dto.response.ChatBot.ChatResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionDetailResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionResponse;

import java.util.List;

public interface ChatService {

    // ---------------------------------------------------------------
    // SESSION
    // ---------------------------------------------------------------

    /** Tạo session mới (customer hoặc guest) */
    ChatSessionResponse createSession(CreateSessionRequest request);

    /** Lấy tất cả session của 1 account */
    List<ChatSessionResponse> getSessionsByAccount(String accountId);

    /** Lấy chi tiết 1 session kèm toàn bộ messages */
    ChatSessionDetailResponse getSessionDetail(String sessionId);

    /** Cập nhật session (đổi title, trạng thái, reset unread) */
    ChatSessionResponse updateSession(String sessionId, UpdateSessionRequest request);

    /** Đóng session */
    ChatSessionResponse closeSession(String sessionId);

    /** Xóa session (soft delete toàn bộ messages bên trong) */
    void deleteSession(String sessionId);

    // ---------------------------------------------------------------
    // MESSAGES
    // ---------------------------------------------------------------

    /** Gửi tin nhắn vào session — bot tự detect intent và sinh reply */
    ChatResponse sendMessage(ChatRequest request);

    /** Lấy toàn bộ messages của 1 session */
    List<ChatResponse> getMessagesBySession(String sessionId);

    /** Thu hồi 1 tin nhắn (soft delete) */
    ChatResponse deleteMessage(String messageId);

    /** Đánh dấu tất cả tin nhắn trong session là đã đọc (admin gọi) */
    void markSessionAsRead(String sessionId);

    // ---------------------------------------------------------------
    // ADMIN
    // ---------------------------------------------------------------

    /** Admin lấy tất cả sessions (phân trang) */
    Page<ChatSessionResponse> getAllSessions(Pageable pageable);

    /** Admin lọc sessions theo status */
    Page<ChatSessionResponse> getSessionsByStatus(int status, Pageable pageable);

    /** Admin gửi reply vào session của customer */
    ChatResponse adminReply(ChatRequest request);
}
