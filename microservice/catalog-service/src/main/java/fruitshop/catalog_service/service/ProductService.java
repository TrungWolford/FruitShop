package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();

    Product findById(String productId);

    Product create(Product product, List<String> categoryIds);

    Product update(String productId, Product product, List<String> categoryIds);

    void delete(String productId);
    
    void updateAverageRatingFromReviewService(String productId);
}
