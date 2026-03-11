package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    private String messageId;

    // Tin nhắn thuộc session nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    // Người gửi (nullable nếu gửi bởi guest hoặc system)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = true)
    private Account sender;

    // Nội dung tin nhắn
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Loại người gửi:
    // "CUSTOMER" = khách hàng
    // "ADMIN"    = quản trị viên
    // "SYSTEM"   = tin nhắn tự động (bot / thông báo)
    @Column(nullable = false)
    private String senderRole;

    // Loại nội dung:
    // "TEXT"  = văn bản thuần
    // "IMAGE" = đường dẫn ảnh
    // "FILE"  = đường dẫn file đính kèm
    @Column(nullable = false)
    private String messageType;

    // Trạng thái tin nhắn:
    // 0 = Đã gửi (Sent)
    // 1 = Đã nhận (Delivered)
    // 2 = Đã đọc (Read)
    private int status;

    // Intent được bot nhận diện từ tin nhắn của user:
    // "PRODUCT_ADVICE"   = tư vấn sản phẩm
    // "PRODUCT_COMPARE"  = so sánh sản phẩm
    // "ORDER_LOOKUP"     = tra cứu đơn hàng
    // "PRODUCT_SUGGEST"  = gợi ý sản phẩm
    // "ORDER_PLACE"      = đặt hàng qua chat
    // "PAYMENT"          = hỏi về thanh toán
    // "GENERAL"          = hỏi thông thường
    // null               = tin nhắn của ADMIN hoặc CUSTOMER (không phải bot)
    private String intent;

    // Metadata dạng JSON — lưu context liên quan đến intent:
    // VD intent=PRODUCT_COMPARE  → {"productIds": ["id1","id2"]}
    // VD intent=ORDER_LOOKUP     → {"orderId": "ORD-001"}
    // VD intent=PRODUCT_SUGGEST  → {"categoryId": "cat-1", "maxPrice": 100000}
    // VD intent=ORDER_PLACE      → {"cartId": "cart-123", "status": "PENDING_CONFIRM"}
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Đánh dấu xóa mềm (soft delete) - true nếu tin nhắn đã bị thu hồi
    private boolean deleted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.messageId == null) {
            this.messageId = UUID.randomUUID().toString();
        }
    }
}
