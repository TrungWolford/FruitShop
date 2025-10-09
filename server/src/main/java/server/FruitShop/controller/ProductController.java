package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.dto.response.Product.ProductResponse;
import server.FruitShop.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Public endpoints - anyone can access
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getAllProduct(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        ProductResponse product = productService.getByProductId(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) List<String> categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "0") long minPrice,
            @RequestParam(required = false, defaultValue = "999999999") long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.filterProduct(categoryId, pageable, status, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchProduct(keywords, pageable);
        return ResponseEntity.ok(products);
    }

    // Admin only endpoints
    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody UpdateProductRequest  request) {
        ProductResponse updatedProduct = productService.updateProduct(request, productId);
        return ResponseEntity.ok(updatedProduct);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/cleanup-images")
    public ResponseEntity<String> cleanupDuplicateImages(@PathVariable String productId) {
        productService.cleanupDuplicateImages(productId);
        return ResponseEntity.ok("Duplicate images cleaned up successfully");
    }

    @GetMapping("/top-10")
    public ResponseEntity<List<ProductResponse>> listTop10ProductByStock(){
        return ResponseEntity.ok(productService.getTopSoldProduct());
    }
}
