package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Payment;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Tìm payment theo status với phân trang
     */
    Page<Payment> findByPaymentStatus(int paymentStatus, Pageable pageable);
    
    /**
     * Tìm payment theo transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Tìm payment theo payment method với phân trang
     */
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);
    
    /**
     * Đếm số payment theo status
     */
    long countByPaymentStatus(int paymentStatus);
    
    /**
     * Tìm payment theo payment method và status
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.paymentStatus = :status")
    Page<Payment> findByMethodAndStatus(
            @Param("method") String method, 
            @Param("status") int status, 
            Pageable pageable
    );
}

