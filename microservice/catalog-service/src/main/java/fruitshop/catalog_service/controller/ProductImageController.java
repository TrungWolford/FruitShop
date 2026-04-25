package fruitshop.catalog_service.controller;

import fruitshop.catalog_service.entity.ProductImage;
import fruitshop.catalog_service.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/product-images")
@RequiredArgsConstructor
public class ProductImageController {
    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImage>> findByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(productImageService.findByProductId(productId));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductImage> create(@PathVariable String productId, @RequestBody ProductImage image) {
        return ResponseEntity.ok(productImageService.create(productId, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
