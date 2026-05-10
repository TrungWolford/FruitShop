package fruitshop.catalog_service.service;

import fruitshop.catalog_service.dto.request.Product.CreateProductRequest;
import fruitshop.catalog_service.dto.request.Product.UpdateProductRequest;
import fruitshop.catalog_service.dto.response.Product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse findById(String productId);

    ProductResponse create(CreateProductRequest request);

    ProductResponse update(String productId, UpdateProductRequest request);

    void delete(String productId);

    void updateAverageRatingFromReviewService(String productId);

    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    Page<ProductResponse> getProductsByCategoryId(String categoryId, Pageable pageable);

    Page<ProductResponse> filterProducts(Long minPrice, Long maxPrice, String categoryId, Pageable pageable);
    
    Page<ProductResponse> filter(List<String> categoryIds, Integer status, Long minPrice, Long maxPrice, Pageable pageable);

    List<ProductResponse> getTop10Products();

    List<ProductResponse> getTop8BestSellingProducts();

    void cleanupDuplicateImages(String productId);

    Page<ProductResponse> getRelatedProducts(String categoryId, String productId, Pageable pageable);

    void decrementStock(String productId, int quantity);
}
