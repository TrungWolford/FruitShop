package fruitshop.catalog_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingCreatedEvent {
    private String ratingId;
    private String productId;
    private double ratingStar;
    private String comment;
}
