package fruitshop.review_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ratings")
@Data
public class Rating {
    @Id
    private String ratingId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String productId;

    private String orderItemId;

    private String comment;
    private double ratingStar;
    private int status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.ratingId == null) {
            this.ratingId = UUID.randomUUID().toString();
        }
    }
}
