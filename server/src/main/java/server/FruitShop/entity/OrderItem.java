package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orderitems")
@Data
public class OrderItem {
    @Id
    private String orderDetailId;

    @ManyToOne
    @JoinColumn(name = "orderid")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "productid")
    private Product product;

    private int quantity;

    private long unitPrice;
}

