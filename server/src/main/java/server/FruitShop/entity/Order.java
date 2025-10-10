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
    private String orderId;

    @ManyToOne
    @JoinColumn(name = "accountid")
    private Account account;

    @OneToOne
    @JoinColumn(name = "shippingid")
    private Shipping shipping;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Date createdAt  ;

    private int status; //0: Huy, 1: Dang van chuyen, 2: Da hoan thanh

    @OneToOne
    @JoinColumn(name = "paymentid")
    private Payment payment;

    private long totalAmount;
}
