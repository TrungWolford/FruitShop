package fruitshop.catalog_service.controller;

import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @GetMapping
  public ResponseEntity<Page<Product>> findAll(org.springframework.data.domain.Pageable pageable) {
    return ResponseEntity.ok(productService.findAll(pageable));
  }

  @GetMapping("/filter")
  public ResponseEntity<Page<Product>> filter(
      @RequestParam(required = false) List<String> categoryIds,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false, defaultValue = "0") long minPrice,
      @RequestParam(required = false, defaultValue = "999999999") long maxPrice,
      org.springframework.data.domain.Pageable pageable) {
    return ResponseEntity.ok(productService.filter(categoryIds, status, minPrice, maxPrice, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> findById(@PathVariable("id") String id) {
    return ResponseEntity.ok(productService.findById(id));
  }

  @PostMapping
  public ResponseEntity<Product> create(
      @RequestBody Product product,
      @RequestParam(value = "categoryIds", required = false) List<String> categoryIds) {
    return ResponseEntity.ok(productService.create(product, categoryIds));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> update(
      @PathVariable("id") String id,
      @RequestBody Product product,
      @RequestParam(value = "categoryIds", required = false) List<String> categoryIds) {
    return ResponseEntity.ok(productService.update(id, product, categoryIds));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") String id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
