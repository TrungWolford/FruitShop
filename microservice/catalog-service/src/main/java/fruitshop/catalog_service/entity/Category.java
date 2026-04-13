package fruitshop.catalog_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    private String categoryId;

    private String categoryName;

    private int status;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    private List<Product> products = new ArrayList<>();

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.categoryId == null) {
            this.categoryId = UUID.randomUUID().toString();
        }
    }
}
