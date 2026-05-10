package fruitshop.catalog_service.service;

import fruitshop.catalog_service.dto.request.Product.CreateProductImageRequest;
import fruitshop.catalog_service.dto.response.Product.ProductImageResponse;

import java.util.List;

public interface ProductImageService {
    List<ProductImageResponse> findByProductId(String productId);

    ProductImageResponse create(String productId, CreateProductImageRequest request);

    void delete(Long imageId);
}
