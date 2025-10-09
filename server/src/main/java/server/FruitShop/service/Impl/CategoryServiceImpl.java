package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Category.CreateCategoryRequest;
import server.FruitShop.dto.request.Category.UpdateCategoryRequest;
import server.FruitShop.dto.response.Category.CategoryResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<CategoryResponse> getAllCategory(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryResponse::fromEntity);
    }

    @Override
    public CategoryResponse getByCategoryId(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        return CategoryResponse.fromEntity(category);
    }

    @Override
    public CategoryResponse createCategoryId(CreateCategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setStatus(request.getStatus());

        categoryRepository.saveAndFlush(category);

        return CategoryResponse.fromEntity(category);
    }

    @Override
    public CategoryResponse updateCategoryId(UpdateCategoryRequest request, String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        category.setCategoryName(request.getCategoryName());
        category.setStatus(request.getStatus());

        categoryRepository.saveAndFlush(category);

        return CategoryResponse.fromEntity(category);
    }

    @Override
    public void deleteCategoryId(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        categoryRepository.delete(category);
    }

    @Override
    public Page<CategoryResponse> searchCategory(String keyword, Pageable pageable) {
        return categoryRepository.findByCategoryName(keyword, pageable).map(CategoryResponse::fromEntity);
    }
}

