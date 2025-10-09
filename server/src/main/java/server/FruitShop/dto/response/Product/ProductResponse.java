package server.FruitShop.dto.response.Product;

import lombok.Data;
import server.FruitShop.dto.response.Category.CategoryResponse;
import server.FruitShop.entity.Product;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());

        // Convert categories to CategoryResponse
        if (product.getCategories() != null) {
            List<CategoryResponse> categoryResponses = product.getCategories().stream()
                    .map(CategoryResponse::fromEntity)
                    .collect(Collectors.toList());
            response.setCategories(categoryResponses);
        }

        // Convert images to ProductImageResponse and sort by imageOrder
        if (product.getImages() != null) {
            List<ProductImageResponse> imageResponses = product.getImages().stream()
                    .map(ProductImageResponse::fromEntity)
                    .sorted((img1, img2) -> Integer.compare(img1.getImageOrder(), img2.getImageOrder()))
                    .collect(Collectors.toList());
            response.setImages(imageResponses);
        }
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setDescription(product.getDescription());
        response.setCreatedAt(product.getCreatedAt());
        response.setStatus(product.getStatus());

        return response;
    }
}


