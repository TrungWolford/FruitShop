package server.FruitShop.dto.request.Product;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String productName;

    private List<String> categoryIds;

    private List<CreateProductImageRequest> images;

    private long price;

    private long stock;

    private String description;

    private int status;
}
