package fruitshop.ai_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummary {
    private String productId;
    private String productName;
    private long price;
    private long stock;
    private String description;
}
