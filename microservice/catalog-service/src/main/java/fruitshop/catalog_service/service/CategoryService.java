package fruitshop.catalog_service.service;

import fruitshop.catalog_service.dto.request.Category.CreateCategoryRequest;
import fruitshop.catalog_service.dto.request.Category.UpdateCategoryRequest;
import fruitshop.catalog_service.dto.response.Category.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> findAll();

    Page<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse findById(String categoryId);

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(String categoryId, UpdateCategoryRequest request);

    void delete(String categoryId);

    Page<CategoryResponse> searchCategory(String keyword, Pageable pageable);
}
