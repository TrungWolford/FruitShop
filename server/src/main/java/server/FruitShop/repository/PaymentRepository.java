package server.FruitShop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
}
