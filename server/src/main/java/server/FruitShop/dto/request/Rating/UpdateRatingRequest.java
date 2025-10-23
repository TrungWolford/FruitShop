package server.FruitShop.dto.request.Rating;

import lombok.Data;

@Data
public class UpdateRatingRequest {
    private String comment;
    private int status;
    private double ratingStar;
}
