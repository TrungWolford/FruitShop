package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.entity.ProductImage;
import fruitshop.catalog_service.repository.ProductImageRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import fruitshop.catalog_service.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductImage> findByProductId(String productId) {
        return productImageRepository.findByProductProductId(productId);
    }

    @Override
    public ProductImage create(String productId, ProductImage image) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        image.setProduct(product);
        return productImageRepository.save(image);
    }

    @Override
    public void delete(Long imageId) {
        productImageRepository.deleteById(imageId);
    }
}
