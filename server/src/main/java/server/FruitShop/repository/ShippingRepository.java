package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Shipping;
import java.util.List;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, String> {

    @Query("SELECT s FROM Shipping s WHERE LOWER(s.receiverName) LIKE LOWER(CONCAT('%', :receiverName, '%'))")
    Page<Shipping> findByReceiverName(@Param("receiverName") String receiverName, Pageable pageable);

    @Query("SELECT s FROM Shipping s WHERE LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    Page<Shipping> findByCity(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT s FROM Shipping s WHERE LOWER(s.receiverPhone) LIKE LOWER(CONCAT('%', :phone, '%'))")
    Page<Shipping> findByReceiverPhone(@Param("phone") String phone, Pageable pageable);

    // Find shippings by account id
    @Query("SELECT s FROM Shipping s WHERE s.account.accountId = :accountId")
    List<Shipping> findByAccountAccountId(@Param("accountId") String accountId);
    
    // Filter by status
    Page<Shipping> findByStatus(int status, Pageable pageable);
    
    // Search methods
    Page<Shipping> findByShippingIdContainingIgnoreCaseOrReceiverNameContainingIgnoreCaseOrReceiverPhoneContaining(
            String shippingId, String receiverName, String receiverPhone, Pageable pageable);
    
    // Combined search and filter
    Page<Shipping> findByShippingIdContainingIgnoreCaseAndStatusOrReceiverNameContainingIgnoreCaseAndStatusOrReceiverPhoneContainingAndStatus(
            String shippingId, int status1, String receiverName, int status2, String receiverPhone, int status3, Pageable pageable);
}
