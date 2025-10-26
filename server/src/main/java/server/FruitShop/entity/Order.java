package server.FruitShop.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @ManyToOne
    @JoinColumn(name = "accountid")
    private Account account;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Shipping shipping;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Date createdAt;

    // Status: 1 = Chờ xác nhận, 2 = Đã xác nhận, 3 = Đang giao, 4 = Giao thành công, 0 = Đã hủy
    private int status;

    @OneToOne
    @JoinColumn(name = "paymentid")
    private Payment payment;

    private long totalAmount;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.orderId == null) {
            this.orderId = java.util.UUID.randomUUID().toString();
        }
    }
}
