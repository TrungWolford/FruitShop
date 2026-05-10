package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.dto.request.Product.CreateProductImageRequest;
import fruitshop.catalog_service.dto.request.Product.CreateProductRequest;
import fruitshop.catalog_service.dto.request.Product.UpdateProductRequest;
import fruitshop.catalog_service.dto.response.Product.ProductResponse;
import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.entity.ProductImage;
import fruitshop.catalog_service.event.ProductDeletedEvent;
import fruitshop.catalog_service.event.ProductUpdatedEvent;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StreamBridge streamBridge;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::fromEntity);
    }

    @Override
    public ProductResponse findById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setStatus(1); // Default status
        product.setSoldQuantity(0L);
        
        product.setCategories(resolveCategories(request.getCategoryIds()));
        
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> images = request.getImages().stream()
                    .map(imgReq -> {
                        ProductImage img = new ProductImage();
                        img.setImageUrl(imgReq.getImageUrl());
                        img.setImageOrder(imgReq.getImageOrder() != null ? imgReq.getImageOrder() : 0);
                        img.setIsMain(imgReq.getIsMain() != null ? imgReq.getIsMain() : false);
                        img.setProduct(product);
                        return img;
                    }).collect(Collectors.toList());
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse update(String productId, UpdateProductRequest request) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        existing.setProductName(request.getProductName());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setDescription(request.getDescription());
        existing.setStatus(request.getStatus());
        existing.setCategories(resolveCategories(request.getCategoryIds()));
        
        // Intelligent Image Update Logic from Monolith
        if (request.getImages() != null) {
            List<ProductImage> currentImages = existing.getImages();
            boolean hasChanges = false;
            
            if (currentImages == null || currentImages.size() != request.getImages().size()) {
                hasChanges = true;
            } else {
                for (int i = 0; i < request.getImages().size(); i++) {
                    if (!request.getImages().get(i).getImageUrl().equals(currentImages.get(i).getImageUrl())) {
                        hasChanges = true;
                        break;
                    }
                }
            }

            if (hasChanges) {
                if (existing.getImages() != null) {
                    existing.getImages().clear();
                } else {
                    existing.setImages(new ArrayList<>());
                }
                
                List<ProductImage> newImages = request.getImages().stream()
                        .map(imgReq -> {
                            ProductImage img = new ProductImage();
                            img.setImageUrl(imgReq.getImageUrl());
                            img.setImageOrder(imgReq.getImageOrder() != null ? imgReq.getImageOrder() : 0);
                            img.setIsMain(imgReq.getIsMain() != null ? imgReq.getIsMain() : false);
                            img.setProduct(existing);
                            return img;
                        }).collect(Collectors.toList());
                existing.getImages().addAll(newImages);
            }
        }

        Product updatedProduct = productRepository.save(existing);
        
        streamBridge.send("productUpdatedSupplier-out-0", new ProductUpdatedEvent(
                updatedProduct.getProductId(),
                updatedProduct.getProductName(),
                updatedProduct.getPrice()
        ));
        
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(String productId) {
        productRepository.deleteById(productId);
        streamBridge.send("productDeletedSupplier-out-0", new ProductDeletedEvent(productId));
    }

    @Override
    public void updateAverageRatingFromReviewService(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        log.info("Updating average rating for product: {} triggered by Rating Event.", productId);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable).map(ProductResponse::fromEntity);
        }
        return productRepository.searchProducts(keyword, pageable).map(ProductResponse::fromEntity);
    }

    @Override
    public Page<ProductResponse> getProductsByCategoryId(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(ProductResponse::fromEntity);
    }

    @Override
    public Page<ProductResponse> filterProducts(Long minPrice, Long maxPrice, String categoryId, Pageable pageable) {
        return productRepository.filterProducts(minPrice, maxPrice, categoryId, pageable).map(ProductResponse::fromEntity);
    }

    @Override
    public Page<ProductResponse> filter(List<String> categoryIds, Integer status, Long minPrice, Long maxPrice, Pageable pageable) {
        return productRepository.filter(categoryIds, status, minPrice, maxPrice, pageable).map(ProductResponse::fromEntity);
    }

    @Override
    public List<ProductResponse> getTop10Products() {
        return productRepository.findTop10BySoldQuantity(PageRequest.of(0, 10))
                .stream().map(ProductResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getTop8BestSellingProducts() {
        return productRepository.findTop8BestSelling(PageRequest.of(0, 8))
                .stream().map(ProductResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cleanupDuplicateImages(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (product.getImages() == null || product.getImages().isEmpty()) return;

        java.util.Map<String, List<ProductImage>> imagesByUrl = product.getImages().stream()
                .collect(Collectors.groupingBy(ProductImage::getImageUrl));

        List<ProductImage> imagesToDelete = new ArrayList<>();
        imagesByUrl.forEach((url, images) -> {
            if (images.size() > 1) {
                imagesToDelete.addAll(images.subList(1, images.size()));
            }
        });

        if (!imagesToDelete.isEmpty()) {
            product.getImages().removeAll(imagesToDelete);
            productRepository.save(product);
        }
    }

    @Override
    public Page<ProductResponse> getRelatedProducts(String categoryId, String productId, Pageable pageable) {
        return productRepository.findRelatedProducts(categoryId, productId, pageable).map(ProductResponse::fromEntity);
    }

    @Override
    @Transactional
    public void decrementStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
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

    private List<Category> resolveCategories(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        return categoryRepository.findAllById(categoryIds);
    }
}
