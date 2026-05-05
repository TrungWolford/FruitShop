package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
@Data
public class ChatSession {

    @Id
    private String sessionId;

    // Khách hàng tạo session (nullable nếu là guest chưa đăng nhập)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    // Tiêu đề cuộc trò chuyện (tự sinh hoặc do user đặt)
    private String title;

    // Trạng thái session:
    // 0 = Đã đóng (Closed)
    // 1 = Đang mở (Open)
    // 2 = Đang chờ admin phản hồi (Pending)
    private int status;

    // Tin nhắn cuối cùng (dùng để preview ở danh sách)
    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    // Số tin nhắn chưa đọc (phía admin)
    private int unreadCount;

    // Intent cuối cùng trong session — giúp bot nhớ ngữ cảnh hội thoại
    // VD: user đang hỏi về sản phẩm → intent = "PRODUCT_ADVICE"
    //     user chuyển sang hỏi đơn  → intent = "ORDER_LOOKUP"
    private String lastIntent;

    // Trạng thái đặt hàng qua bot (nếu user đang trong flow đặt hàng):
    // null             = chưa vào flow đặt hàng
    // "SELECTING"      = đang chọn sản phẩm
    // "CONFIRMING"     = đang xác nhận đơn
    // "PAYING"         = đang chọn thanh toán
    // "DONE"           = đặt hàng hoàn tất
    private String orderFlowState;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ 1-nhiều với ChatMessage (cascade: xóa session → xóa tất cả messages)
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.sessionId == null) {
            this.sessionId = UUID.randomUUID().toString();
        }
    }
}
