package fruitshop.catalog_service.dto.response.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import fruitshop.catalog_service.entity.ProductImage;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Integer imageOrder;
    private Boolean isMain;

    public static ProductImageResponse fromEntity(ProductImage productImage){
        if (productImage == null) return null;
        ProductImageResponse response = new ProductImageResponse();
        response.setId(productImage.getId());
        response.setImageUrl(productImage.getImageUrl());
        response.setImageOrder(productImage.getImageOrder());
        response.setIsMain(productImage.getIsMain());
        return response;
    }
}
