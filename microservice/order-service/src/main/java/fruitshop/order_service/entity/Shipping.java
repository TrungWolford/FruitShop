package fruitshop.order_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shippings")
@Data
public class Shipping {
    @Id
    private String shippingId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    private String accountId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String city;

    private String shipperName;

    private long shippingFee;

    private Instant shippedAt;

    private int status;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.shippingId == null) {
            this.shippingId = UUID.randomUUID().toString();
        }
    }
}
