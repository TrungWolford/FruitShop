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

    @OneToOne
    @JoinColumn(name = "orderid", unique = true)
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

    // Status: 1 = Đang chuẩn bị, 2 = Đang giao, 3 = Giao thành công, 0 = Đã hủy
    private int status;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.shippingId == null) {
            this.shippingId = java.util.UUID.randomUUID().toString();
        }
    }
}
