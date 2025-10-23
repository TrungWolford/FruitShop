package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.dto.response.Rating.RatingResponse;

public interface RatingService {
    Page<RatingResponse> getAllRating(Pageable pageable);
    Page<RatingResponse> getRatingsByAccountId(String accountId, Pageable pageable);
    Page<RatingResponse> getRatingsByProductId(String productId, Pageable pageable);
    RatingResponse getRatingsByAccountIdAndProductId(String accountId, String productId);
    RatingResponse createRating(CreateRatingRequest request);
    RatingResponse updateRating(UpdateRatingRequest request, String ratingId);
    RatingResponse changeStatus(String ratingId);

    double calculateRatingStarByProductId(String productId);
}
