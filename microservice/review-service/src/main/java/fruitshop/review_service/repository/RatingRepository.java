package fruitshop.review_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fruitshop.review_service.entity.Rating;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    Page<Rating> findAll(Pageable pageable);
    Page<Rating> findByAccountAccountId(Pageable pageable, String accountId);
    Page<Rating> findByProductProductId(Pageable pageable, String productId);
    Page<Rating> findByProductProductIdAndStatus(String productId, Integer status, Pageable pageable);
    List<Rating> findByAccountAccountIdAndProductProductId(String accountId, String productId);
    List<Rating> findByProductProductId(String productId);
    Optional<Rating> findByOrderItemOrderDetailId(String orderDetailId);
}