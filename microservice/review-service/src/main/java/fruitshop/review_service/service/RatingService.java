package fruitshop.review_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.dto.request.Rating.UpdateRatingRequest;
import fruitshop.review_service.dto.response.Rating.RatingResponse;
import fruitshop.review_service.dto.response.Rating.RatingDetailResponse;

import java.util.List;

public interface RatingService {
    Page<RatingResponse> getAllRating(Pageable pageable);
    Page<RatingDetailResponse> getAllRatingDetailed(Pageable pageable);
    Page<RatingDetailResponse> getRatingsByAccountId(String accountId, Pageable pageable);
    Page<RatingDetailResponse> getRatingsByProductId(String productId, Pageable pageable);
    List<RatingDetailResponse> getRatingsByAccountIdAndProductId(String accountId, String productId);
    RatingResponse createRating(CreateRatingRequest request);
    RatingResponse updateRating(UpdateRatingRequest request, String ratingId);
    RatingResponse changeStatus(String ratingId);
    double calculateRatingStarByProductId(String productId);
    RatingResponse getRatingById(String ratingId);
    void deleteRating(String ratingId);
    long countRatingsByProductId(String productId);
}
