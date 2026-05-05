package fruitshop.review_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingUpdatedEvent {
    private String ratingId;
    private String productId;
    private double ratingStar;
    private int status;
}
