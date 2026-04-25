package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    @Override
    public Product create(Product product, List<String> categoryIds) {
        product.setCategories(resolveCategories(categoryIds));
        return productRepository.save(product);
    }

    @Override
    public Product update(String productId, Product product, List<String> categoryIds) {
        Product existing = findById(productId);
        existing.setProductName(product.getProductName());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        existing.setStatus(product.getStatus());
        existing.setCategories(resolveCategories(categoryIds));
        return productRepository.save(existing);
    }

    @Override
    public void delete(String productId) {
        productRepository.deleteById(productId);
    }

    private List<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return categoryRepository.findAllById(categoryIds);
    }
}
