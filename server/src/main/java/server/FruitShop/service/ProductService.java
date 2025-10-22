package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.dto.response.Product.ProductResponse;

import java.util.List;

public interface ProductService {
    public Page<ProductResponse> getAllProduct(Pageable pageable);

    public ProductResponse getByProductId(String productId);

    public ProductResponse createProduct(CreateProductRequest request);

    public ProductResponse updateProduct(UpdateProductRequest request, String productId);

    public void deleteProduct(String productId);

    public Page<ProductResponse> filterProduct(List<String> categoryId, Pageable pageable, Integer status, long minPrice, long maxPrice);

    public Page<ProductResponse> searchProduct(String keywords, Double minPrice, Double maxPrice, Pageable pageable);

    public List<ProductResponse> getTopSoldProduct();

    // Cleanup duplicate images
    public void cleanupDuplicateImages(String productId);
}
