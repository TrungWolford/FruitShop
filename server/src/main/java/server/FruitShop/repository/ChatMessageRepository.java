package server.FruitShop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    // Lấy toàn bộ tin nhắn của 1 session (sắp xếp theo thời gian)
    List<ChatMessage> findByChatSession_SessionIdOrderByCreatedAtAsc(String sessionId);

    // Lấy tin nhắn chưa bị xóa mềm của 1 session
    List<ChatMessage> findByChatSession_SessionIdAndDeletedFalseOrderByCreatedAtAsc(String sessionId);

    // Đếm tin nhắn chưa đọc (status < 2) trong 1 session gửi bởi CUSTOMER
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatSession.sessionId = :sessionId AND m.senderRole = 'CUSTOMER' AND m.status < 2")
    long countUnreadBySessionId(@Param("sessionId") String sessionId);

    // Lấy tin nhắn cuối cùng của session
    @Query("SELECT m FROM ChatMessage m WHERE m.chatSession.sessionId = :sessionId ORDER BY m.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageBySessionId(@Param("sessionId") String sessionId);

    // Đánh dấu tất cả tin nhắn CUSTOMER trong session là đã đọc (status = 2)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 2 WHERE m.chatSession.sessionId = :sessionId AND m.senderRole = 'CUSTOMER' AND m.status < 2")
    int markAllAsReadBySessionId(@Param("sessionId") String sessionId);

    // Lấy tin nhắn theo intent trong 1 session (để phân tích context)
    List<ChatMessage> findByChatSession_SessionIdAndIntentOrderByCreatedAtAsc(String sessionId, String intent);

    // Xóa mềm tất cả tin nhắn của session khi session bị xóa
    @Modifying
    @Query("UPDATE ChatMessage m SET m.deleted = true WHERE m.chatSession.sessionId = :sessionId")
    int softDeleteAllBySessionId(@Param("sessionId") String sessionId);

    // Lấy N tin nhắn gần nhất của session (cho context memory)
    @Query(value = """
            SELECT * FROM chat_messages 
            WHERE session_id = :sessionId AND deleted = false
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ChatMessage> findRecentMessagesBySessionId(
            @Param("sessionId") String sessionId,
            @Param("limit") int limit);
}
