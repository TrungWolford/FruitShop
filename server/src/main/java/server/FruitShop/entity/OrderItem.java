package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orderitems")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderDetailId;

    @ManyToOne
    @JoinColumn(name = "orderid")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "productid")
    private Product product;

    private int quantity;

    private long unitPrice;

    // Status của từng orderItem (null: normal, "returned": đã trả hàng, "returning": đang xử lý trả hàng)
    private String status;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.orderDetailId == null) {
            this.orderDetailId = java.util.UUID.randomUUID().toString();
        }
    }
}

