package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> findAll() {
    return categoryRepository.findAll();
  }

  @Override
  public Category findById(String categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
  }

  @Override
  public Category create(Category category) {
    return categoryRepository.save(category);
  }

  @Override
  public Category update(String categoryId, Category category) {
    Category existing = findById(categoryId);
    existing.setCategoryName(category.getCategoryName());
    existing.setStatus(category.getStatus());
    return categoryRepository.save(existing);
  }

  @Override
  public void delete(String categoryId) {
    categoryRepository.deleteById(categoryId);
  }

}

  
  
    
  