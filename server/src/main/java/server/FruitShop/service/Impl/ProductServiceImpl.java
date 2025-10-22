package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.dto.response.Product.ProductResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.ProductImage;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.repository.ProductImageRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.service.ProductService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public Page<ProductResponse> getAllProduct(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);

        List<String> productIds = productsPage.getContent().stream()
                .map(Product::getProductId)
                .toList();

        List<Product> productsWithCategories = productRepository.findByIdsWithCategories(productIds);

        // Tạo map để lookup nhanh
        Map<String, Product> productMap = productsWithCategories.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // Map page content với categories và đảm bảo collections được load
        List<ProductResponse> responses = productsPage.getContent().stream()
                .map(product -> {
                    Product productWithCat = productMap.getOrDefault(product.getProductId(), product);


                    ProductResponse response = ProductResponse.fromEntity(productWithCat);

                    return response;
                })
                .collect(Collectors.toList());

        // Tạo lại Page với responses
        return new PageImpl<>(responses, pageable, productsPage.getTotalElements());
    }

    @Override
    public ProductResponse getByProductId(String productId) {
        // Load product with categories first
        Product product = productRepository.findByIdWithCategories(productId);
        if (product == null) {
            throw new RuntimeException("Product not found: " + productId);
        }

        // Then load images separately to avoid MultipleBagFetchException
        Product productWithImages = productRepository.findByIdWithImages(productId);
        if (productWithImages != null) {
            product.setImages(productWithImages.getImages());
        }

        if (product.getImages() != null) {
            // Check for duplicates
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .toList();
            Set<String> uniqueUrls = new HashSet<>(imageUrls);
            if (imageUrls.size() != uniqueUrls.size()) {
                System.out.println("WARNING: Duplicate image URLs detected!");
                System.out.println("  Total images: " + imageUrls.size());
                System.out.println("  Unique URLs: " + uniqueUrls.size());
            }
        }

        return ProductResponse.fromEntity(product);
    }

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

        // Lưu product trước (không có categories để tránh lỗi constraint)
        Product savedProduct = productRepository.save(product);

        // Xử lý categories sau khi product đã có ID
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Remove duplicates
            List<String> uniqueCategoryIds = request.getCategoryIds().stream()
                    .distinct()
                    .toList();
            
            List<Category> categories = categoryRepository.findAllById(uniqueCategoryIds);
            
            // Validate all categories exist
            if (categories.size() != uniqueCategoryIds.size()) {
                throw new RuntimeException("Some categories were not found");
            }
            
            // Set categories và save lại
            savedProduct.setCategories(categories);
            savedProduct = productRepository.save(savedProduct);
        }

        // Xử lý images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            final Product finalProduct = savedProduct; // Make final for lambda
            List<ProductImage> images = request.getImages().stream()
                    .map(imageRequest -> {
                        ProductImage image = new ProductImage();
                        image.setImageUrl(imageRequest.getImageUrl());
                        image.setImageOrder(imageRequest.getImageOrder() != null ? imageRequest.getImageOrder() : 0);
                        image.setIsMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false);
                        image.setProduct(finalProduct);
                        return image;
                    })
                    .collect(Collectors.toList());

            productImageRepository.saveAll(images);
            savedProduct.setImages(images);
        }

        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UpdateProductRequest request, String productId) {
        try {
            System.out.println("🔄 Updating product: " + productId);
            System.out.println("📦 Request data: " + request);

            // Load product with categories first
            Product product = productRepository.findByIdWithCategories(productId);
            if (product == null) {
                throw new RuntimeException("Product not found: " + productId);
            }

            // Then load images separately to avoid MultipleBagFetchException
            Product productWithImages = productRepository.findByIdWithImages(productId);
            if (productWithImages != null) {
                product.setImages(productWithImages.getImages());
            }

            product.setProductName(request.getProductName());

            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setDescription(request.getDescription());
            product.setUpdatedAt(new Date());
            product.setStatus(request.getStatus());

            // Xử lý categories từ categoryIds
            if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                // Remove duplicates by converting to Set
                List<String> uniqueCategoryIds = request.getCategoryIds().stream()
                        .distinct()
                        .toList();
                
                List<Category> categories = categoryRepository.findAllById(uniqueCategoryIds);
                
                // Validate all categories exist
                if (categories.size() != uniqueCategoryIds.size()) {
                    throw new RuntimeException("Some categories were not found");
                }
                
                // Clear existing categories and add new ones
                product.getCategories().clear();
                product.getCategories().addAll(categories);
            }

            // Xử lý images thông minh - chỉ update khi có thay đổi
            if (request.getImages() != null) {
                // Lấy danh sách images hiện tại
                List<ProductImage> currentImages = product.getImages();

                // So sánh để xem có thay đổi không
                boolean hasChanges = false;
                if (currentImages == null || currentImages.size() != request.getImages().size()) {
                    hasChanges = true;
                } else {
                    // Kiểm tra từng image xem có thay đổi không
                    for (int i = 0; i < request.getImages().size(); i++) {
                        String newImageUrl = request.getImages().get(i).getImageUrl();
                        if (!newImageUrl.equals(currentImages.get(i).getImageUrl())) {
                            hasChanges = true;
                            break;
                        }
                    }
                }

                if (hasChanges) {

                    // Clear existing images properly to avoid orphan deletion issue
                    if (currentImages != null) {
                        currentImages.clear(); // Clear collection instead of setting new one
                    }

                    // Xóa images cũ từ database
                    productImageRepository.deleteByProductProductId(productId);

                    // Thêm images mới nếu có
                    if (!request.getImages().isEmpty()) {
                        List<ProductImage> newImages = request.getImages().stream()
                                .map(imageRequest -> {
                                    ProductImage image = new ProductImage();
                                    image.setImageUrl(imageRequest.getImageUrl());
                                    image.setImageOrder(imageRequest.getImageOrder() != null ? imageRequest.getImageOrder() : 0);
                                    image.setIsMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false);
                                    image.setProduct(product);
                                    return image;
                                })
                                .collect(Collectors.toList());

                        // Add to existing collection instead of setting new collection
                        if (product.getImages() != null) {
                            product.getImages().addAll(newImages);
                        } else {
                            product.setImages(newImages);
                        }

                        productImageRepository.saveAll(newImages);
                    }
                }
            }

            productRepository.saveAndFlush(product);
            return ProductResponse.fromEntity(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Xóa tất cả images trước
        productImageRepository.deleteByProductProductId(productId);

        productRepository.delete(product);
    }

    @Override
    public Page<ProductResponse> filterProduct(List<String> categoryId, Pageable pageable, Integer status, long minPrice, long maxPrice) {
        Page<Product> productsPage;

        // Nếu có cả categoryId và status
        if (categoryId != null && !categoryId.isEmpty() && status != null) {
            productsPage = productRepository.findProductsByCategoryIdsAndStatusAndInRangePrice(categoryId, status, minPrice, maxPrice, pageable);
        }
        // Nếu chỉ có status
        else if (status != null) {
            productsPage = productRepository.findProductsByCategoryStatusAndInRangePrice(status, minPrice, maxPrice, pageable);
        }
        // Nếu chỉ có categoryId
        else if (categoryId != null && !categoryId.isEmpty()) {
            productsPage = productRepository.findProductsByCategoryIdsAndInRangePrice(categoryId, minPrice, maxPrice, pageable);
        }
        // Nếu không có gì thì return tất cả với price filter
        else {
            productsPage = productRepository.findAllByPriceRange(minPrice, maxPrice, pageable);
        }

        List<String> productIds = productsPage.getContent().stream()
                .map(Product::getProductId)
                .toList();

        List<Product> productsWithCategories = productRepository.findByIdsWithCategories(productIds);

        Map<String, Product> productMap = productsWithCategories.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // Map page content với categories và đảm bảo collections được load
        List<ProductResponse> responses = productsPage.getContent().stream()
                .map(product -> {
                    Product productWithCat = productMap.getOrDefault(product.getProductId(), product);
                    return ProductResponse.fromEntity(productWithCat);
                })
                .collect(Collectors.toList());

        // Tạo lại Page với responses
        return new PageImpl<>(responses, pageable, productsPage.getTotalElements());
    }

    @Override
    public Page<ProductResponse> searchProduct(String keywords, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByProductName(keywords, pageable);
        
        // Filter by price if provided
        if (minPrice != null || maxPrice != null) {
            List<Product> filteredProducts = products.getContent().stream()
                    .filter(product -> {
                        boolean matchMin = minPrice == null || product.getPrice() >= minPrice;
                        boolean matchMax = maxPrice == null || product.getPrice() <= maxPrice;
                        return matchMin && matchMax;
                    })
                    .toList();
            
            // Create new Page with filtered results
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());
            List<Product> pageContent = filteredProducts.subList(start, end);
            
            return new org.springframework.data.domain.PageImpl<>(
                    pageContent.stream().map(ProductResponse::fromEntity).toList(),
                    pageable,
                    filteredProducts.size()
            );
        }
        
        return products.map(ProductResponse::fromEntity);
    }

    @Override
    public List<ProductResponse> getTopSoldProduct() {
        return productRepository.findTop10ByOrderByStockAsc().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    // Method to cleanup duplicate images for a product
    @Transactional
    public void cleanupDuplicateImages(String productId) {
        try {
            // Load product with images for cleanup
            Product product = productRepository.findByIdWithImages(productId);
            if (product == null || product.getImages() == null) {
                return;
            }

            System.out.println("🧹 Cleaning up duplicate images for product: " + productId);

            // Group images by URL and keep only the first one for each URL
            Map<String, List<ProductImage>> imagesByUrl = product.getImages().stream()
                    .collect(Collectors.groupingBy(ProductImage::getImageUrl));

            List<ProductImage> imagesToKeep = new ArrayList<>();
            List<ProductImage> imagesToDelete = new ArrayList<>();

            imagesByUrl.forEach((url, images) -> {
                if (images.size() > 1) {
                    // Keep the first one, delete the rest
                    imagesToKeep.add(images.get(0));
                    imagesToDelete.addAll(images.subList(1, images.size()));
                } else {
                    imagesToKeep.add(images.get(0));
                }
            });

            if (!imagesToDelete.isEmpty()) {
                productImageRepository.deleteAll(imagesToDelete);

                // Update product with cleaned images
                product.setImages(imagesToKeep);
                productRepository.save(product);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

