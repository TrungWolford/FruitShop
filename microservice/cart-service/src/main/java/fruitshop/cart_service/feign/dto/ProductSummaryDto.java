package fruitshop.cart_service.feign.dto;

import lombok.Data;

@Data
public class ProductSummaryDto {
    private String productId;
    private int status;
    private long stock;
}
