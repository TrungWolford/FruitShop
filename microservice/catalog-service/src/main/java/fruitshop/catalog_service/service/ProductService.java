package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.Product;

import java.util.List;

public interface ProductService {
  org.springframework.data.domain.Page<Product> findAll(org.springframework.data.domain.Pageable pageable);

  org.springframework.data.domain.Page<Product> filter(List<String> categoryIds, Integer status, long minPrice, long maxPrice, org.springframework.data.domain.Pageable pageable);

  Product findById(String productId);

  Product create(Product product, List<String> categoryIds);

  Product update(String productId, Product product, List<String> categoryIds);

  void delete(String productId);
}
