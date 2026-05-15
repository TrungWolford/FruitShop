package fruitshop.catalog_service.service.impl;

import fruitshop.catalog_service.dto.request.Category.CreateCategoryRequest;
import fruitshop.catalog_service.dto.request.Category.UpdateCategoryRequest;
import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_savesCategoryAndReturnsResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setCategoryName("Trai cay nhiet doi");
        request.setStatus(1);

        Category saved = new Category();
        saved.setCategoryId("cat-1");
        saved.setCategoryName("Trai cay nhiet doi");
        saved.setStatus(1);

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var response = categoryService.create(request);

        assertEquals("cat-1", response.getCategoryId());
        assertEquals("Trai cay nhiet doi", response.getCategoryName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void findAll_withPagination_returnsPagedResults() {
        var pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setCategoryId("cat-2");
        category.setCategoryName("Tao");
        category.setStatus(1);

        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category), pageable, 1));

        var result = categoryService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("cat-2", result.getContent().get(0).getCategoryId());
    }

    @Test
    void findById_categoryExists_returnsData() {
        Category category = new Category();
        category.setCategoryId("cat-1");
        category.setCategoryName("Trai cay nhiet doi");
        category.setStatus(1);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        var response = categoryService.findById("cat-1");

        assertEquals("cat-1", response.getCategoryId());
        assertEquals("Trai cay nhiet doi", response.getCategoryName());
    }

    @Test
    void update_categoryFound_updatesSuccessfully() {
        Category existing = new Category();
        existing.setCategoryId("cat-1");
        existing.setCategoryName("Old Name");
        existing.setStatus(1);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(existing);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("New Name");
        request.setStatus(1);

        var response = categoryService.update("cat-1", request);

        assertEquals("cat-1", response.getCategoryId());
        verify(categoryRepository).save(existing);
    }

    @Test
    void delete_categoryExists_deletesSuccessfully() {
        categoryService.delete("cat-1");
        verify(categoryRepository).deleteById("cat-1");
    }
}
