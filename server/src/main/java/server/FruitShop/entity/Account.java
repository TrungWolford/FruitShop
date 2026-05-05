package server.FruitShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    private String accountId;

    private String accountName;

    private String accountPhone;

    private String password;

    private int status; // 0: Ngung hoat dong, 1: Dang hoat dong

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    public void generateIdIfAbsent() {
        if (this.accountId == null) {
            this.accountId = UUID.randomUUID().toString();
        }
    }
}

