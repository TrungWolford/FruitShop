package fruitshop.order_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Data
public class Refund {
    @Id
    private String refundId;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private String reason;

    private String refundStatus;

    private Instant requestedAt;

    private Instant processedAt;

    private long refundAmount;

    private String imageUrls;

    private String originalPaymentId;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.refundId == null) {
            this.refundId = UUID.randomUUID().toString();
        }
        if (this.requestedAt == null) {
            this.requestedAt = Instant.now();
        }
    }
}
