package fruitshop.review_service.dto.request.Rating;

import lombok.Data;

@Data
public class CreateRatingRequest {
    private String accountId;
    private String productId;
    private String orderItemId;
    private String comment;
    private int ratingStar;
}
