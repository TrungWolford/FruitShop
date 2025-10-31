package server.FruitShop.dto.request.Rating;

import lombok.Data;

@Data
public class CreateRatingRequest {
    private String accountId;
    private String productId;
    private String orderItemId; // Link rating to specific order item
    private String comment;
    private int ratingStar;
}
