package fruitshop.catalog_service.dto.response.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import fruitshop.catalog_service.dto.response.Category.CategoryResponse;
import fruitshop.catalog_service.entity.Product;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String productId;
    private String productName;
    private List<CategoryResponse> categories;
    private List<ProductImageResponse> images;
    private long price;
    private long stock;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private int status;

    public static ProductResponse fromEntity(Product product){
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());

        if (product.getCategories() != null) {
            response.setCategories(product.getCategories().stream()
                    .map(CategoryResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (product.getImages() != null) {
            response.setImages(product.getImages().stream()
                    .map(ProductImageResponse::fromEntity)
                    .sorted((img1, img2) -> {
                        Integer order1 = img1.getImageOrder() != null ? img1.getImageOrder() : Integer.MAX_VALUE;
                        Integer order2 = img2.getImageOrder() != null ? img2.getImageOrder() : Integer.MAX_VALUE;
                        return Integer.compare(order1, order2);
                    })
                    .collect(Collectors.toList()));
        }
        
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setDescription(product.getDescription());
        response.setCreatedAt(product.getCreatedAt() != null ? Date.from(product.getCreatedAt()) : null);
        response.setUpdatedAt(product.getUpdatedAt() != null ? Date.from(product.getUpdatedAt()) : null);
        response.setStatus(product.getStatus());

        return response;
    }
}
