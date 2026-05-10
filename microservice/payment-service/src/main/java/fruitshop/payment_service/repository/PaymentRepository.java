package fruitshop.payment_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import fruitshop.payment_service.entity.Payment;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Page<Payment> findByPaymentStatus(int paymentStatus, Pageable pageable);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrderId(String orderId);
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);
    long countByPaymentStatus(int paymentStatus);

    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.paymentStatus = :status")
    Page<Payment> findByMethodAndStatus(@Param("method") String method, @Param("status") int status, Pageable pageable);
}