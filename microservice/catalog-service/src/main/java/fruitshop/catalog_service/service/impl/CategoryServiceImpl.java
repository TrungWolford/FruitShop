package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.dto.request.Category.CreateCategoryRequest;
import fruitshop.catalog_service.dto.request.Category.UpdateCategoryRequest;
import fruitshop.catalog_service.dto.response.Category.CategoryResponse;
import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(CategoryResponse::fromEntity);
    }

    @Override
    public CategoryResponse findById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setStatus(request.getStatus());
        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(String categoryId, UpdateCategoryRequest request) {
        Category existing = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        existing.setCategoryName(request.getCategoryName());
        existing.setStatus(request.getStatus());
        return CategoryResponse.fromEntity(categoryRepository.save(existing));
    }

    @Override
    public void delete(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public Page<CategoryResponse> searchCategory(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pageable).map(CategoryResponse::fromEntity);
        }
        return categoryRepository.searchCategory(keyword, pageable).map(CategoryResponse::fromEntity);
    }
}
