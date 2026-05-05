package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Page<Category> getAllCategory(Pageable pageable);

    Category findById(String categoryId);

    Category create(Category category);

    Category update(String categoryId, Category category);

    void delete(String categoryId);

    Page<Category> searchCategory(String keyword, Pageable pageable);
}
