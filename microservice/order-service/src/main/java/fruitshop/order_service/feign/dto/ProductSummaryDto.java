package fruitshop.order_service.feign.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductSummaryDto {
    private String productId;
    private String productName;
    private long price;
    private int status;
    private List<String> images;
}
