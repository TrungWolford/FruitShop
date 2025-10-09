package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    private String productId;

    private String productName;

    @OneToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "productid"),
            inverseJoinColumns = @JoinColumn(name = "categoryid")
    )
    private List<Category> categories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    private long price;

    private long stock;

    private String description;

    private Date createdAt;

    private Date updatedAt;

    private int status; // 0: Ngung hoat dong, 1: Dang hoat dong
}