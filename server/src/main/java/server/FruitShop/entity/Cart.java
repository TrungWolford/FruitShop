package server.FruitShop.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "carts")
@Data
public class Cart {
    @Id
    private String cartId;

    @OneToOne
    @JoinColumn(name = "accountid")
    private Account account;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items = new ArrayList<>();

    private Date createdAt;

    private int status;

}

