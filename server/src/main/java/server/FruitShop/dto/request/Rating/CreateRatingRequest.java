package server.FruitShop.dto.request.Rating;

import lombok.Data;

@Data
public class CreateRatingRequest {
    private String accountId;
    private String productId;
    private String comment;
    private int ratingStar;
}
