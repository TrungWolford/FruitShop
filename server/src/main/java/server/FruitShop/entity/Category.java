package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    private String categoryId;

    private String categoryName;

    private int status; // 0: Huy, 1: Dang hoat dong

    @ManyToMany(mappedBy = "categories")
    private List<Product> products;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.categoryId == null) {
            this.categoryId = UUID.randomUUID().toString();
        }
    }
}
