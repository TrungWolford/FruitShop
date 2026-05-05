package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<Product> findAll();

    Page<Product> getAllProducts(Pageable pageable);

    Product findById(String productId);

    Product create(Product product, List<String> categoryIds);

    Product update(String productId, Product product, List<String> categoryIds);

    void delete(String productId);
    
    void updateAverageRatingFromReviewService(String productId);

    Page<Product> searchProducts(String keyword, Pageable pageable);

    Page<Product> getProductsByCategoryId(String categoryId, Pageable pageable);

    Page<Product> filterProducts(Long minPrice, Long maxPrice, String categoryId, Pageable pageable);

    List<Product> getTop8BestSellingProducts();

    Page<Product> getRelatedProducts(String categoryId, String productId, Pageable pageable);

    void decrementStock(String productId, int quantity);
}
