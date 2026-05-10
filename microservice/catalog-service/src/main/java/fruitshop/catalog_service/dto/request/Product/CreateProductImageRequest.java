package fruitshop.catalog_service.dto.request.Product;

import lombok.Data;

@Data
public class CreateProductImageRequest {
    private String imageUrl;
    private Integer imageOrder;
    private Boolean isMain;
}
