package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Shipping;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, String> {

    @Query("SELECT s FROM Shipping s WHERE LOWER(s.receiverName) LIKE LOWER(CONCAT('%', :receiverName, '%'))")
    Page<Shipping> findByReceiverName(@Param("receiverName") String receiverName, Pageable pageable);

    @Query("SELECT s FROM Shipping s WHERE LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    Page<Shipping> findByCity(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT s FROM Shipping s WHERE LOWER(s.receiverPhone) LIKE LOWER(CONCAT('%', :phone, '%'))")
    Page<Shipping> findByReceiverPhone(@Param("phone") String phone, Pageable pageable);
}
