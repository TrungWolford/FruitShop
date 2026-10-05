package server.FruitShop.dto.response.Product;

import lombok.Data;
import server.FruitShop.dto.response.Category.CategoryResponse;
import server.FruitShop.entity.Product;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    /** Map từ entity đã được fetch đầy đủ categories + images. */
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setDescription(product.getDescription());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setStatus(product.getStatus());

        if (product.getCategories() != null) {
            response.setCategories(
                    product.getCategories().stream()
                            .map(CategoryResponse::fromEntity)
                            .toList()
            );
        } else {
            response.setCategories(Collections.emptyList());
        }

        if (product.getImages() != null) {
            response.setImages(
                    product.getImages().stream()
                            .map(ProductImageResponse::fromEntity)
                            .sorted((a, b) -> Integer.compare(
                                    a.getImageOrder() != null ? a.getImageOrder() : Integer.MAX_VALUE,
                                    b.getImageOrder() != null ? b.getImageOrder() : Integer.MAX_VALUE))
                            .toList()
            );
        } else {
            response.setImages(Collections.emptyList());
        }

        return response;
    }

    /**
     * Assemble từ 2 entity riêng biệt:
     * - {@code withCategories}: product đã fetch categories
     * - {@code withImages}: product đã fetch images
     * Dùng cho single-product lookup (getByProductId, updateProduct).
     */
    public static ProductResponse fromEntities(Product withCategories, Product withImages) {
        ProductResponse response = fromEntity(withCategories);
        if (withImages != null && withImages.getImages() != null) {
            response.setImages(
                    withImages.getImages().stream()
                            .map(ProductImageResponse::fromEntity)
                            .sorted((a, b) -> Integer.compare(
                                    a.getImageOrder() != null ? a.getImageOrder() : Integer.MAX_VALUE,
                                    b.getImageOrder() != null ? b.getImageOrder() : Integer.MAX_VALUE))
                            .toList()
            );
        }
        return response;
    }
}
