package server.FruitShop.dto.response.Rating;

import lombok.Data;
import server.FruitShop.entity.Rating;

import java.time.LocalDateTime;

@Data
public class RatingResponse {
    private String ratingId;
    private String accountId;
    private String productId;
    private String comment;
    private int status;
    private double ratingStar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RatingResponse fromEntity(Rating rating){
        RatingResponse response = new RatingResponse();
        response.setRatingId(rating.getRatingId());
        response.setAccountId(rating.getAccount().getAccountId());
        response.setProductId(rating.getProduct().getProductId());
        response.setComment(rating.getComment());
        response.setStatus(rating.getStatus());
        response.setRatingStar(rating.getRatingStar());
        response.setCreatedAt(rating.getCreatedAt());
        response.setUpdatedAt(rating.getUpdatedAt());

        return response;
    }
}
