package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.event.ProductDeletedEvent;
import fruitshop.catalog_service.event.ProductUpdatedEvent;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StreamBridge streamBridge;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product findById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    @Override
    public Product create(Product product, List<String> categoryIds) {
        product.setCategories(resolveCategories(categoryIds));
        if (product.getImages() != null) {
            product.getImages().forEach(img -> img.setProduct(product));
        }
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
        
        // Cleanup and replace images
        if (product.getImages() != null) {
            existing.getImages().clear();
            existing.getImages().addAll(product.getImages());
            existing.getImages().forEach(img -> img.setProduct(existing));
        }

        Product updatedProduct = productRepository.save(existing);
        
        streamBridge.send("productUpdatedSupplier-out-0", new ProductUpdatedEvent(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getPrice()
        ));
        
        return updatedProduct;
    }

    @Override
    public void delete(String productId) {
        productRepository.deleteById(productId);
        streamBridge.send("productDeletedSupplier-out-0", new ProductDeletedEvent(productId));
    }

    @Override
    public void updateAverageRatingFromReviewService(String productId) {
        Product product = findById(productId);
        System.out.println("Updating average rating for product: " + productId + " triggered by Rating Event.");
        productRepository.save(product);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public Page<Product> getProductsByCategoryId(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> filterProducts(Long minPrice, Long maxPrice, String categoryId, Pageable pageable) {
        return productRepository.filterProducts(minPrice, maxPrice, categoryId, pageable);
    }

    @Override
    public List<Product> getTop8BestSellingProducts() {
        return productRepository.findTop8BestSelling(org.springframework.data.domain.PageRequest.of(0, 8));
    }

    @Override
    public Page<Product> getRelatedProducts(String categoryId, String productId, Pageable pageable) {
        return productRepository.findRelatedProducts(categoryId, productId, pageable);
    }

    private List<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return categoryRepository.findAllById(categoryIds);
    }

    @Override
    @Transactional
    public void decrementStock(String productId, int quantity) {
        Product product = findById(productId);
        long currentStock = product.getStock();
        if (currentStock < quantity) {
            log.warn("Stock insufficient for product {}. Current: {}, Requested: {}. Setting stock to 0.", productId, currentStock, quantity);
            product.setStock(0L);
        } else {
            product.setStock(currentStock - (long) quantity);
        }
        product.setSoldQuantity(product.getSoldQuantity() + (long) quantity);
        productRepository.save(product);
        log.info("Decremented stock for product {} by {}. New stock: {}, New soldQuantity: {}", 
                productId, quantity, product.getStock(), product.getSoldQuantity());
    }
}
