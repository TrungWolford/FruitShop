package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "ratings")
@Data
public class Rating {
    @Id
    private String ratingId;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String comment;

    private double ratingStar;

    private int status;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.ratingId == null) {
            this.ratingId = UUID.randomUUID().toString();
        }
    }
}
