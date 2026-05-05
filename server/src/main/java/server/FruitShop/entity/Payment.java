package server.FruitShop.entity;

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
    private String paymentMethod; // COD, Bank Transfer, E-Wallet, Credit Card, etc.

    @Column(name = "paymentStatus")
    private int paymentStatus; // 0: Pending, 1: Completed, 2: Failed, 3: Refunded

    @Column(name = "paymentDate")
    private Date paymentDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transactionId")
    private String transactionId; // ID giao dịch từ cổng thanh toán

    @PrePersist
    public void generateId() {
        if (this.paymentId == null) {
            this.paymentId = UUID.randomUUID().toString();
        }
    }
}
