package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.ProductImage;

import java.util.List;

public interface ProductImageService {
    List<ProductImage> findByProductId(String productId);

    ProductImage create(String productId, ProductImage image);

    void delete(Long imageId);
}
