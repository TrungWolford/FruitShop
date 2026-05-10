package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.dto.request.Product.CreateProductImageRequest;
import fruitshop.catalog_service.dto.response.Product.ProductImageResponse;
import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.entity.ProductImage;
import fruitshop.catalog_service.repository.ProductImageRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import fruitshop.catalog_service.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductImageResponse> findByProductId(String productId) {
        return productImageRepository.findByProductProductId(productId).stream()
                .map(ProductImageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageResponse create(String productId, CreateProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        ProductImage image = new ProductImage();
        image.setImageUrl(request.getImageUrl());
        image.setImageOrder(request.getImageOrder());
        image.setIsMain(request.getIsMain());
        image.setProduct(product);
        
        return ProductImageResponse.fromEntity(productImageRepository.save(image));
    }

    @Override
    public void delete(Long imageId) {
        productImageRepository.deleteById(imageId);
    }
}
