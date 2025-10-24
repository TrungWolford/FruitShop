package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "shippings")
@Data
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String shippingId;

    @OneToOne(mappedBy = "shipping")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "accountid")
    private Account account;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String city;

    private long shippingFee;

    private Date shippedAt;

    private int status;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.shippingId == null) {
            this.shippingId = java.util.UUID.randomUUID().toString();
        }
    }
}
