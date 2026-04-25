package fruitshop.catalog_service.controller;

import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody Product product,
            @RequestParam(value = "categoryIds", required = false) List<String> categoryIds
    ) {
        return ResponseEntity.ok(productService.create(product, categoryIds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable("id") String id,
            @RequestBody Product product,
            @RequestParam(value = "categoryIds", required = false) List<String> categoryIds
    ) {
        return ResponseEntity.ok(productService.update(id, product, categoryIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
