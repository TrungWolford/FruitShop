package fruitshop.payment_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "paymentMethod", length = 50)
    private String paymentMethod;

    @Column(name = "paymentStatus")
    private int paymentStatus;

    @Column(name = "paymentDate")
    private Date paymentDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transactionId")
    private String transactionId;

    @PrePersist
    public void generateId() {
        if (this.paymentId == null) {
            this.paymentId = UUID.randomUUID().toString();
        }
    }
}