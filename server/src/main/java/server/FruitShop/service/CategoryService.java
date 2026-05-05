package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Category.CreateCategoryRequest;
import server.FruitShop.dto.request.Category.UpdateCategoryRequest;
import server.FruitShop.dto.response.Category.CategoryResponse;

public interface CategoryService {
    public Page<CategoryResponse> getAllCategory(Pageable pageable);

    public CategoryResponse getByCategoryId(String categoryId);

    public CategoryResponse createCategoryId(CreateCategoryRequest request);

    public CategoryResponse updateCategoryId(UpdateCategoryRequest request, String categoryId);

    public void deleteCategoryId(String categoryId);

    public Page<CategoryResponse> searchCategory(String keyword, Pageable pageable);

}
