package fruitshop.catalog_service.controller;

import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<Category>> findAll() {
    return ResponseEntity.ok(categoryService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Category> findById(@PathVariable("id") String id) {
    return ResponseEntity.ok(categoryService.findById(id));
  }

  @PostMapping
  public ResponseEntity<Category> create(@RequestBody Category category) {
    return ResponseEntity.ok(categoryService.create(category));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Category> update(@PathVariable("id") String id, @RequestBody Category category) {
    return ResponseEntity.ok(categoryService.update(id, category));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") String id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
