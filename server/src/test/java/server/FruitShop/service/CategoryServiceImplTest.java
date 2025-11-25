package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Category.CreateCategoryRequest;
import server.FruitShop.dto.request.Category.UpdateCategoryRequest;
import server.FruitShop.dto.response.Category.CategoryResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.service.Impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CategoryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Category Service")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setCategoryId("cat-001");
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Test 1: Lấy tất cả categories - Thành công")
    void testGetAllCategory_Success() {
        // ARRANGE
        List<Category> categories = List.of(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // ACT
        Page<CategoryResponse> result = categoryService.getAllCategory(pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Trái cây nhiệt đới", result.getContent().get(0).getCategoryName());
        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test 2: Lấy category theo ID - Thành công")
    void testGetByCategoryId_Success() {
        // ARRANGE
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));

        // ACT
        CategoryResponse result = categoryService.getByCategoryId("cat-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("cat-001", result.getCategoryId());
        assertEquals("Trái cây nhiệt đới", result.getCategoryName());
        verify(categoryRepository, times(1)).findById("cat-001");
    }

    @Test
    @DisplayName("Test 3: Lấy category theo ID - Không tìm thấy")
    void testGetByCategoryId_NotFound() {
        // ARRANGE
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getByCategoryId("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 4: Tạo category mới - Thành công")
    void testCreateCategoryId_Success() {
        // ARRANGE
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setCategoryName("Rau củ quả");
        request.setStatus(1);

        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(testCategory);

        // ACT
        CategoryResponse result = categoryService.createCategoryId(request);

        // ASSERT
        assertNotNull(result);
        verify(categoryRepository, times(1)).saveAndFlush(any(Category.class));
    }

    @Test
    @DisplayName("Test 5: Cập nhật category - Thành công")
    void testUpdateCategoryId_Success() {
        // ARRANGE
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Trái cây nhập khẩu");
        request.setStatus(1);

        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(testCategory);

        // ACT
        CategoryResponse result = categoryService.updateCategoryId(request, "cat-001");

        // ASSERT
        assertNotNull(result);
        verify(categoryRepository, times(1)).findById("cat-001");
        verify(categoryRepository, times(1)).saveAndFlush(any(Category.class));
    }

    @Test
    @DisplayName("Test 6: Cập nhật category - Không tìm thấy")
    void testUpdateCategoryId_NotFound() {
        // ARRANGE
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Test");
        request.setStatus(1);

        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategoryId(request, "invalid-id");
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id");
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    @DisplayName("Test 7: Xóa category - Thành công")
    void testDeleteCategoryId_Success() {
        // ARRANGE
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        // ACT
        categoryService.deleteCategoryId("cat-001");

        // ASSERT
        verify(categoryRepository, times(1)).findById("cat-001");
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    @DisplayName("Test 8: Xóa category - Không tìm thấy")
    void testDeleteCategoryId_NotFound() {
        // ARRANGE
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategoryId("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id");
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Test 9: Tìm kiếm category - Thành công")
    void testSearchCategory_Success() {
        // ARRANGE
        String keyword = "Trái cây";
        List<Category> categories = List.of(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findByCategoryName(keyword, pageable)).thenReturn(categoryPage);

        // ACT
        Page<CategoryResponse> result = categoryService.searchCategory(keyword, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository, times(1)).findByCategoryName(keyword, pageable);
    }

    @Test
    @DisplayName("Test 10: Tìm kiếm category - Không có kết quả")
    void testSearchCategory_NoResults() {
        // ARRANGE
        String keyword = "NotExist";
        Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(categoryRepository.findByCategoryName(keyword, pageable)).thenReturn(emptyPage);

        // ACT
        Page<CategoryResponse> result = categoryService.searchCategory(keyword, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(categoryRepository, times(1)).findByCategoryName(keyword, pageable);
    }
}
