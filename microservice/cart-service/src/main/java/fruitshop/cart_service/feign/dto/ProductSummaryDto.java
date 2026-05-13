package fruitshop.cart_service.feign.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductSummaryDto {
    private String productId;
    private String productName;
    private long price;
    private int status;
    private long stock;
    private List<ProductImageDto> images;

    @Data
    public static class ProductImageDto {
        private String imageId;
        private String imageUrl;
        private Integer imageOrder;
    }
}
