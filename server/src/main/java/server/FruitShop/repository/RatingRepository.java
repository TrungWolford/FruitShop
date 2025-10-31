package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Rating;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    Page<Rating> findAll(Pageable pageable);
    Page<Rating> findByAccountAccountId(Pageable pageable, String accountId);
    Page<Rating> findByProductProductId(Pageable pageable, String productId);
    
    // Find ratings by productId and status (for active/visible ratings only)
    Page<Rating> findByProductProductIdAndStatus(String productId, Integer status, Pageable pageable);
    
    // Find all ratings by account and product (can return multiple ratings)
    List<Rating> findByAccountAccountIdAndProductProductId(String accountId, String productId);
    
    // Find all ratings by productId without pagination
    List<Rating> findByProductProductId(String productId);
    
    // Find rating by orderItem's orderDetailId (each orderItem can have only 1 rating)
    Optional<Rating> findByOrderItemOrderDetailId(String orderDetailId);
}
