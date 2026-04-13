package fruitshop.review_service.feign.dto;

import lombok.Data;

@Data
public class ProductSummaryDto {
    private String productId;
    private String productName;
    private int status;
}
