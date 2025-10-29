package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.dto.response.Rating.RatingResponse;
import server.FruitShop.dto.response.Rating.RatingDetailResponse;

public interface RatingService {
    Page<RatingResponse> getAllRating(Pageable pageable);
    Page<RatingDetailResponse> getAllRatingDetailed(Pageable pageable);
    Page<RatingDetailResponse> getRatingsByAccountId(String accountId, Pageable pageable);
    Page<RatingDetailResponse> getRatingsByProductId(String productId, Pageable pageable);
    RatingDetailResponse getRatingsByAccountIdAndProductId(String accountId, String productId);
    RatingResponse createRating(CreateRatingRequest request);
    RatingResponse updateRating(UpdateRatingRequest request, String ratingId);
    RatingResponse changeStatus(String ratingId);

    double calculateRatingStarByProductId(String productId);
}
