package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "refunds")
@Data
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String refundId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;  // Add orderItem reference for per-item refund

    private String reason;          // Lý do trả hàng / hoàn tiền
    private String refundStatus;    // Pending, Approved, Rejected, Completed
    private Date requestedAt;       // Ngày người mua yêu cầu
    private Date processedAt;       // Ngày hoàn tất xử lý
    private long refundAmount;      // Số tiền hoàn
    
    private String imageUrls;       // JSON array of image URLs (for evidence)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id")
    private Payment originalPayment; // Giao dịch thanh toán gốc (nếu cần liên kết)
}

