package fruitshop.catalog_service.service;

import fruitshop.catalog_service.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Category findById(String categoryId);

    Category create(Category category);

    Category update(String categoryId, Category category);

    void delete(String categoryId);
}
