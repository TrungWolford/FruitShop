package server.FruitShop.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    private String roleId;

    private String roleName;

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.roleId == null) {
            this.roleId = UUID.randomUUID().toString();
        }
    }
}
