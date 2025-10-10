package server.FruitShop.dto.response.Product;

import lombok.Data;
import server.FruitShop.entity.ProductImage;

@Data
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Integer imageOrder;
    private Boolean isMain;

    public static ProductImageResponse fromEntity(ProductImage productImage){
        ProductImageResponse response = new ProductImageResponse();
        response.setId(productImage.getId());
        response.setImageUrl(productImage.getImageUrl());
        response.setImageOrder(productImage.getImageOrder());
        response.setIsMain(productImage.getIsMain());

        return  response;
    }
}
