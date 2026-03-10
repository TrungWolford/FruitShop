package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.ChatSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    // Lấy tất cả session của 1 account (sắp xếp mới nhất trước)
    List<ChatSession> findByAccount_AccountIdOrderByUpdatedAtDesc(String accountId);

    // Lấy session theo accountId + status (VD: chỉ lấy session đang mở)
    List<ChatSession> findByAccount_AccountIdAndStatusOrderByUpdatedAtDesc(String accountId, int status);

    // Lấy tất cả session (admin xem toàn bộ) — phân trang
    Page<ChatSession> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    // Lấy session theo status — phân trang (admin lọc theo trạng thái)
    Page<ChatSession> findByStatusOrderByUpdatedAtDesc(int status, Pageable pageable);

    // Lấy session kèm messages (tránh N+1 query)
    @Query("SELECT DISTINCT s FROM ChatSession s LEFT JOIN FETCH s.messages WHERE s.sessionId = :sessionId")
    Optional<ChatSession> findByIdWithMessages(@Param("sessionId") String sessionId);

    // Đếm số session đang chờ phản hồi (admin dashboard)
    long countByStatus(int status);

    // Lấy session theo accountId có phân trang
    @Query("SELECT s FROM ChatSession s WHERE s.account.accountId = :accountId ORDER BY s.updatedAt DESC")
    Page<ChatSession> findByAccountId(@Param("accountId") String accountId, Pageable pageable);
}
