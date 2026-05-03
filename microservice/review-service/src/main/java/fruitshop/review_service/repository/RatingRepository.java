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
    Page<Rating> findByAccountId(String accountId, Pageable pageable);
    Page<Rating> findByProductId(String productId, Pageable pageable);
    Page<Rating> findByProductIdAndStatus(String productId, Integer status, Pageable pageable);
    List<Rating> findByAccountIdAndProductId(String accountId, String productId);
    List<Rating> findByProductId(String productId);
    Optional<Rating> findByOrderItemId(String orderItemId);
}
