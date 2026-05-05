package fruitshop.order_service.repository;

import fruitshop.order_service.entity.Shipping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShippingRepository extends JpaRepository<Shipping, String> {
    Optional<Shipping> findByOrderOrderId(String orderId);

    List<Shipping> findByAccountId(String accountId);

    Page<Shipping> findByStatus(int status, Pageable pageable);

    @Query("SELECT s FROM Shipping s WHERE " +
            "LOWER(s.shippingId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.receiverPhone LIKE CONCAT('%', :keyword, '%')")
    Page<Shipping> searchShippings(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Shipping s WHERE " +
            "(LOWER(s.shippingId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.receiverPhone LIKE CONCAT('%', :keyword, '%')) " +
            "AND s.status = :status")
    Page<Shipping> searchAndFilterShippings(@Param("keyword") String keyword, @Param("status") int status, Pageable pageable);
}
