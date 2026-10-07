package server.FruitShop.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Product.CreateProductImageRequest;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.dto.response.Product.ProductResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.ProductImage;
import server.FruitShop.exception.ResourceNotFoundException;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.repository.ProductImageRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.service.ProductService;
import server.FruitShop.service.TwoLevelCacheService;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl — cache strategy:
 *
 * getByProductId  → Two-level (L1 Caffeine + L2 Redis) thủ công qua ProductCacheService
 *                   Lý do: cần full control (warm L1 từ L2, evict cả hai layer khi update/delete)
 *
 * getTopSoldProduct → @Cacheable("top-products") — Caffeine L1 + Redis L2 thủ công
 *                     Lý do: list thay đổi ít, chịu được stale 5 phút
 *
 * Write ops (create/update/delete) → evict cache liên quan
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private static final String TOP_PRODUCTS_REDIS_KEY = "fruitshop:top-products";
    private static final Duration TOP_PRODUCTS_TTL     = Duration.ofMinutes(5);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    /** Two-level cache cho single product detail */
    private final TwoLevelCacheService<ProductResponse> productCacheService;

    /** RedisTemplate để cache top-products list */
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository,
                              @Qualifier("productCacheService")
                              TwoLevelCacheService<ProductResponse> productCacheService,
                              RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productCacheService = productCacheService;
        this.redisTemplate = redisTemplate;
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProduct(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        if (page.isEmpty()) return Page.empty(pageable);
        return toResponsePage(page, pageable);
    }

    /**
     * getByProductId — Two-level cache (L1 Caffeine + L2 Redis).
     *
     * Flow:
     *   1. Tìm L1 (Caffeine) → hit → return
     *   2. Tìm L2 (Redis)    → hit → warm L1 → return
     *   3. DB query          → put vào cả L1 + L2
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByProductId(String productId) {
        // Bước 1 & 2: L1 → L2
        ProductResponse cached = productCacheService.get(productId);
        if (cached != null) {
            return cached;
        }

        // Bước 3: DB
        log.debug("[Cache MISS] getByProductId:{} → query DB", productId);
        Product withCategories = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Product withImages = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductResponse response = ProductResponse.fromEntities(withCategories, withImages);

        // Nạp vào cả L1 và L2
        productCacheService.put(productId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProduct(List<String> categoryId, Pageable pageable,
                                               Integer status, long minPrice, long maxPrice) {
        Page<Product> page;

        if (categoryId != null && !categoryId.isEmpty() && status != null) {
            page = productRepository.findProductsByCategoryIdsAndStatusAndInRangePrice(
                    categoryId, status, minPrice, maxPrice, pageable);
        } else if (status != null) {
            page = productRepository.findProductsByCategoryStatusAndInRangePrice(
                    status, minPrice, maxPrice, pageable);
        } else if (categoryId != null && !categoryId.isEmpty()) {
            page = productRepository.findProductsByCategoryIdsAndInRangePrice(
                    categoryId, minPrice, maxPrice, pageable);
        } else {
            page = productRepository.findAllByPriceRange(minPrice, maxPrice, pageable);
        }

        if (page.isEmpty()) return Page.empty(pageable);
        return toResponsePage(page, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProduct(String keywords, Double minPrice, Double maxPrice,
                                               Pageable pageable) {
        Page<Product> page;

        if (minPrice != null || maxPrice != null) {
            double lo = minPrice != null ? minPrice : 0;
            double hi = maxPrice != null ? maxPrice : Double.MAX_VALUE;
            page = productRepository.findByProductNameAndPriceRange(keywords, lo, hi, pageable);
        } else {
            page = productRepository.findByProductName(keywords, pageable);
        }

        if (page.isEmpty()) return Page.empty(pageable);
        return toResponsePage(page, pageable);
    }

    /**
     * getTopSoldProduct — Two-level cache (L1 @Cacheable Caffeine + L2 Redis thủ công).
     *
     * @Cacheable("top-products") xử lý L1 (Caffeine via CaffeineCacheManager).
     * Redis L2 được kiểm tra trước khi xuống DB khi L1 miss.
     */
    @Override
    @Cacheable("top-products")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ProductResponse> getTopSoldProduct() {
        // L1: Spring @Cacheable sẽ intercept nếu đã có trong Caffeine — xử lý bên ngoài
        // L2: thủ công check Redis
        Object raw = redisTemplate.opsForValue().get(TOP_PRODUCTS_REDIS_KEY);
        if (raw instanceof List<?> list && !list.isEmpty()) {
            log.debug("[Cache L2 HIT] top-products");
            return (List<ProductResponse>) list;
        }

        log.debug("[Cache MISS] top-products → query DB");

        // Native query trả về Product không fetch lazy collections (categories, images)
        // → cần second-pass batch fetch cả categories lẫn images
        List<Product> rawProducts = productRepository.findTop10BySoldQuantity();
        if (rawProducts.isEmpty()) return List.of();

        List<String> ids = rawProducts.stream().map(Product::getProductId).toList();

        // Second-pass: batch load categories
        Map<String, Product> categoriesMap = productRepository.findAllWithCategoriesByIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // Second-pass: batch load images
        Map<String, Product> imagesMap = productRepository.findAllWithImagesByIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<ProductResponse> result = rawProducts.stream()
                .map(p -> {
                    Product withCat   = categoriesMap.get(p.getProductId());
                    Product withImg   = imagesMap.get(p.getProductId());
                    // Dùng withCat làm base (có categories), merge images từ withImg
                    return ProductResponse.fromEntities(
                            withCat != null ? withCat : p,
                            withImg
                    );
                })
                .toList();

        // Nạp vào L2 Redis
        redisTemplate.opsForValue().set(TOP_PRODUCTS_REDIS_KEY, result, TOP_PRODUCTS_TTL);
        log.debug("[Cache PUT] top-products → Redis TTL={}", TOP_PRODUCTS_TTL);

        return result;
    }

    // =========================================================================
    // WRITE — evict cache sau mỗi thao tác ghi
    // =========================================================================

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        product.setStatus(1);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<String> uniqueIds = request.getCategoryIds().stream().distinct().toList();
            List<Category> categories = categoryRepository.findAllById(uniqueIds);
            if (categories.size() != uniqueIds.size()) {
                throw new ResourceNotFoundException("Some categories were not found");
            }
            product.setCategories(categories);
        }

        Product saved = productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> images = buildImages(request.getImages(), saved);
            productImageRepository.saveAll(images);
            saved.setImages(images);
        }

        // top-products list đã thay đổi → evict L2
        evictTopProductsCache();

        return ProductResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UpdateProductRequest request, String productId) {
        Product product = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Product productWithImages = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        product.setImages(productWithImages.getImages());

        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setUpdatedAt(new Date());
        product.setStatus(request.getStatus());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<String> uniqueIds = request.getCategoryIds().stream().distinct().toList();
            List<Category> categories = categoryRepository.findAllById(uniqueIds);
            if (categories.size() != uniqueIds.size()) {
                throw new ResourceNotFoundException("Some categories were not found");
            }
            product.getCategories().clear();
            product.getCategories().addAll(categories);
        }

        if (request.getImages() != null && imagesChanged(product.getImages(), request.getImages())) {
            productImageRepository.deleteByProductProductId(productId);
            product.getImages().clear();
            if (!request.getImages().isEmpty()) {
                List<ProductImage> newImages = buildImages(request.getImages(), product);
                productImageRepository.saveAll(newImages);
                product.getImages().addAll(newImages);
            }
        }

        productRepository.saveAndFlush(product);

        // Evict cache của product này + top-products
        productCacheService.evict(productId);
        evictTopProductsCache();
        log.debug("[Cache EVICT] after updateProduct:{}", productId);

        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        productImageRepository.deleteByProductProductId(productId);
        productRepository.delete(product);

        // Evict cache
        productCacheService.evict(productId);
        evictTopProductsCache();
        log.debug("[Cache EVICT] after deleteProduct:{}", productId);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Assemble Page<ProductResponse> từ Page<Product> (categories đã fetch sẵn).
     * Batch-fetch images: tổng 2 queries O(1).
     */
    private Page<ProductResponse> toResponsePage(Page<Product> page, Pageable pageable) {
        List<String> ids = page.getContent().stream().map(Product::getProductId).toList();

        Map<String, Product> imagesMap = productRepository.findAllWithImagesByIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<ProductResponse> responses = page.getContent().stream()
                .map(p -> ProductResponse.fromEntities(p, imagesMap.get(p.getProductId())))
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    private boolean imagesChanged(List<ProductImage> current,
                                  List<CreateProductImageRequest> requested) {
        if (current == null || current.size() != requested.size()) return true;
        for (int i = 0; i < requested.size(); i++) {
            if (!requested.get(i).getImageUrl().equals(current.get(i).getImageUrl())) return true;
        }
        return false;
    }

    private List<ProductImage> buildImages(List<CreateProductImageRequest> imageRequests,
                                           Product product) {
        return imageRequests.stream().map(req -> {
            ProductImage img = new ProductImage();
            img.setImageUrl(req.getImageUrl());
            img.setImageOrder(req.getImageOrder() != null ? req.getImageOrder() : 0);
            img.setIsMain(req.getIsMain() != null ? req.getIsMain() : false);
            img.setProduct(product);
            return img;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cleanupDuplicateImages(String productId) {
        Product product = productRepository.findByIdWithImages(productId).orElse(null);
        if (product == null || product.getImages() == null) return;

        Map<String, List<ProductImage>> imagesByUrl = product.getImages().stream()
                .collect(Collectors.groupingBy(ProductImage::getImageUrl));

        List<ProductImage> toDelete = new ArrayList<>();
        imagesByUrl.values().forEach(imgs -> {
            if (imgs.size() > 1) {
                toDelete.addAll(imgs.subList(1, imgs.size()));
            }
        });

        if (!toDelete.isEmpty()) {
            productImageRepository.deleteAll(toDelete);
            productCacheService.evict(productId); // cache stale sau cleanup
        }
    }

    /** Xóa top-products khỏi Redis L2. */
    private void evictTopProductsCache() {
        redisTemplate.delete(TOP_PRODUCTS_REDIS_KEY);
        log.debug("[Cache EVICT] top-products Redis key");
    }
}
