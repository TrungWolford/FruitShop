package fruitshop.catalog_service.controller;

import fruitshop.catalog_service.dto.request.Product.CreateProductRequest;
import fruitshop.catalog_service.dto.request.Product.UpdateProductRequest;
import fruitshop.catalog_service.dto.response.Product.ProductResponse;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @GetMapping
  public ResponseEntity<Page<ProductResponse>> findAll(org.springframework.data.domain.Pageable pageable) {
    return ResponseEntity.ok(productService.getAllProducts(pageable));
  }

  @GetMapping("/filter")
  public ResponseEntity<Page<ProductResponse>> filter(
      @RequestParam(required = false) List<String> categoryIds,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false, defaultValue = "0") long minPrice,
      @RequestParam(required = false, defaultValue = "999999999") long maxPrice,
      org.springframework.data.domain.Pageable pageable) {
    return ResponseEntity.ok(productService.filter(categoryIds, status, minPrice, maxPrice, pageable));
  }

  @GetMapping("/paginated")
  public ResponseEntity<Page<ProductResponse>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(productService.getAllProducts(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable("id") String id) {
    return ResponseEntity.ok(productService.findById(id));
  }

  @PostMapping
  public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
    return ResponseEntity.ok(productService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable("id") String id,
      @RequestBody UpdateProductRequest request) {
    return ResponseEntity.ok(productService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") String id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ProductResponse>> searchProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
  }

  @GetMapping("/category/{categoryId}")
  public ResponseEntity<Page<ProductResponse>> getProductsByCategoryId(
      @PathVariable String categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId, pageable));
  }

  @GetMapping("/filter/paged")
  public ResponseEntity<Page<ProductResponse>> filterProducts(
      @RequestParam(required = false) Long minPrice,
      @RequestParam(required = false) Long maxPrice,
      @RequestParam(required = false) String categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(productService.filterProducts(minPrice, maxPrice, categoryId, pageable));
  }

  @GetMapping("/top-selling")
  public ResponseEntity<List<ProductResponse>> getTop8BestSellingProducts() {
    return ResponseEntity.ok(productService.getTop8BestSellingProducts());
  }

  @GetMapping("/top-10")
  public ResponseEntity<List<ProductResponse>> getTop10Products() {
    return ResponseEntity.ok(productService.getTop10Products());
  }

  @PostMapping("/{productId}/cleanup-images")
  public ResponseEntity<String> cleanupDuplicateImages(@PathVariable String productId) {
    productService.cleanupDuplicateImages(productId);
    return ResponseEntity.ok("Duplicate images cleaned up successfully");
  }

  @GetMapping("/{productId}/related")
  public ResponseEntity<Page<ProductResponse>> getRelatedProducts(
      @PathVariable String productId,
      @RequestParam String categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "4") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return ResponseEntity.ok(productService.getRelatedProducts(categoryId, productId, pageable));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(ex.getMessage());
  }
}
