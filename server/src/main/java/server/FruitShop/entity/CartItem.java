package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cartitems")
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cartItemId;

    @ManyToOne
    @JoinColumn(name = "cartid")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productid")
    private Product product;

    private int quantity;

}

