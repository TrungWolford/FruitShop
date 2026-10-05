package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
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

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByProductId(String productId) {
        Product withCategories = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Product withImages = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        return ProductResponse.fromEntities(withCategories, withImages);
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

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopSoldProduct() {
        // categories đã được fetch cùng query qua @EntityGraph
        List<Product> withCategories = productRepository.findTop10WithCategoriesOrderByStockAsc();
        if (withCategories.isEmpty()) return List.of();

        // Batch-fetch images riêng (1 query IN) để tránh MultipleBagFetchException
        List<String> ids = withCategories.stream().map(Product::getProductId).toList();
        Map<String, Product> imagesMap = productRepository.findAllWithImagesByIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        return withCategories.stream()
                .map(p -> ProductResponse.fromEntities(p, imagesMap.get(p.getProductId())))
                .toList();
    }

    // =========================================================================
    // WRITE
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

        return ProductResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UpdateProductRequest request, String productId) {
        // 1 query với categories, 1 query với images – tổng 2 queries
        Product product = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Product productWithImages = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        product.setImages(productWithImages.getImages());

        // Cập nhật scalar fields
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setUpdatedAt(new Date());
        product.setStatus(request.getStatus());

        // Cập nhật categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<String> uniqueIds = request.getCategoryIds().stream().distinct().toList();
            List<Category> categories = categoryRepository.findAllById(uniqueIds);
            if (categories.size() != uniqueIds.size()) {
                throw new ResourceNotFoundException("Some categories were not found");
            }
            product.getCategories().clear();
            product.getCategories().addAll(categories);
        }

        // Cập nhật images nếu có thay đổi
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
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        productImageRepository.deleteByProductProductId(productId);
        productRepository.delete(product);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Assemble Page<ProductResponse> từ một Page<Product> mà categories đã được
     * fetch sẵn ở tầng repo (qua @EntityGraph). Chỉ cần thêm 1 query batch-fetch images.
     * Tổng số queries: 1 (page + categories, repo) + 1 (IN images) = O(1).
     */
    private Page<ProductResponse> toResponsePage(Page<Product> page, Pageable pageable) {
        List<String> ids = page.getContent().stream().map(Product::getProductId).toList();

        // Batch-fetch images riêng (tránh MultipleBagFetchException)
        Map<String, Product> imagesMap = productRepository.findAllWithImagesByIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        List<ProductResponse> responses = page.getContent().stream()
                .map(p -> ProductResponse.fromEntities(p, imagesMap.get(p.getProductId())))
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    /** Kiểm tra xem danh sách images từ request có khác với images hiện tại không. */
    private boolean imagesChanged(List<ProductImage> current,
                                  List<CreateProductImageRequest> requested) {
        if (current == null || current.size() != requested.size()) return true;
        for (int i = 0; i < requested.size(); i++) {
            if (!requested.get(i).getImageUrl().equals(current.get(i).getImageUrl())) return true;
        }
        return false;
    }

    /** Tạo danh sách ProductImage từ request. */
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
    /** Xoá ảnh trùng lặp cho một sản phẩm – implement ProductService interface. */
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
        }
    }
}
