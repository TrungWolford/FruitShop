package server.FruitShop.dto.request.Product;

import lombok.Data;

@Data
public class CreateProductImageRequest {
    private String imageUrl;
    private Integer imageOrder;
}
