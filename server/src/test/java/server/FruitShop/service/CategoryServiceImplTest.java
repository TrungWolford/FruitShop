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
 * Class này test tất cả các chức năng của CategoryService
 * sử dụng Mockito để mock các dependencies
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework cho JUnit 5
@DisplayName("Unit Test - Category Service")
class CategoryServiceImplTest {

    @Mock // Tạo mock object cho CategoryRepository
    private CategoryRepository categoryRepository;

    @InjectMocks // Inject các mock objects vào CategoryServiceImpl
    private CategoryServiceImpl categoryService;

    // Dữ liệu test mẫu
    private Category testCategory;
    private Pageable pageable;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Method này chạy trước mỗi @Test method
     */
    @BeforeEach
    void setUp() {
        // Tạo một category mẫu để test
        testCategory = new Category();
        testCategory.setCategoryId("cat-001");
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1); // 1 = Active

        // Tạo Pageable với page 0, size 10
        pageable = PageRequest.of(0, 10);
    }

    /**
     * Test case 1: Kiểm tra lấy danh sách tất cả categories
     * Kịch bản: Lấy tất cả categories với phân trang
     * Kết quả mong đợi: Trả về Page chứa danh sách categories
     */
    @Test
    @DisplayName("Test 1: Lấy tất cả categories - Thành công")
    void testGetAllCategory_Success() {
        // ARRANGE - Chuẩn bị dữ liệu test
        List<Category> categories = List.of(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        
        // Mock repository trả về page chứa 1 category
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // ACT - Thực hiện hành động cần test
        Page<CategoryResponse> result = categoryService.getAllCategory(pageable);

        // ASSERT - Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        assertEquals(1, result.getTotalElements()); // Có đúng 1 phần tử
        assertEquals("Trái cây nhiệt đới", result.getContent().get(0).getCategoryName()); // Tên category đúng
        verify(categoryRepository, times(1)).findAll(pageable); // Verify repository được gọi 1 lần
    }

    /**
     * Test case 2: Kiểm tra lấy category theo ID
     * Kịch bản: Tìm category với ID tồn tại
     * Kết quả mong đợi: Trả về CategoryResponse với thông tin đầy đủ
     */
    @Test
    @DisplayName("Test 2: Lấy category theo ID - Thành công")
    void testGetByCategoryId_Success() {
        // ARRANGE - Mock repository trả về category khi tìm theo ID
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));

        // ACT - Gọi service để lấy category
        CategoryResponse result = categoryService.getByCategoryId("cat-001");

        // ASSERT - Kiểm tra các thuộc tính của category trả về
        assertNotNull(result);
        assertEquals("cat-001", result.getCategoryId()); // ID đúng
        assertEquals("Trái cây nhiệt đới", result.getCategoryName()); // Tên đúng
        verify(categoryRepository, times(1)).findById("cat-001"); // Repository được gọi đúng 1 lần
    }

    /**
     * Test case 3: Kiểm tra lấy category với ID không tồn tại
     * Kịch bản: Tìm category với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException với message "Category not found"
     */
    @Test
    @DisplayName("Test 3: Lấy category theo ID - Không tìm thấy")
    void testGetByCategoryId_NotFound() {
        // ARRANGE - Mock repository trả về empty khi không tìm thấy
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Kiểm tra exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getByCategoryId("invalid-id");
        });

        // Verify exception message chứa "Category not found"
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id");
    }

    /**
     * Test case 4: Kiểm tra tạo category mới
     * Kịch bản: Tạo category mới với thông tin hợp lệ
     * Kết quả mong đợi: Category được lưu vào database và trả về CategoryResponse
     */
    @Test
    @DisplayName("Test 4: Tạo category mới - Thành công")
    void testCreateCategoryId_Success() {
        // ARRANGE - Tạo request object với thông tin category mới
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setCategoryName("Rau củ quả");
        request.setStatus(1); // Status active

        // Mock repository lưu và trả về category
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(testCategory);

        // ACT - Gọi service để tạo category
        CategoryResponse result = categoryService.createCategoryId(request);

        // ASSERT - Verify kết quả và repository được gọi
        assertNotNull(result); // Kết quả không null
        verify(categoryRepository, times(1)).saveAndFlush(any(Category.class)); // Save được gọi 1 lần
    }

    /**
     * Test case 5: Kiểm tra cập nhật category
     * Kịch bản: Cập nhật thông tin category đã tồn tại
     * Kết quả mong đợi: Category được cập nhật và trả về CategoryResponse mới
     */
    @Test
    @DisplayName("Test 5: Cập nhật category - Thành công")
    void testUpdateCategoryId_Success() {
        // ARRANGE - Tạo request với thông tin cập nhật
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Trái cây nhập khẩu"); // Tên mới
        request.setStatus(1);

        // Mock repository tìm thấy category và lưu thành công
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(testCategory);

        // ACT - Gọi service để cập nhật
        CategoryResponse result = categoryService.updateCategoryId(request, "cat-001");

        // ASSERT - Verify kết quả và các method được gọi đúng
        assertNotNull(result);
        verify(categoryRepository, times(1)).findById("cat-001"); // Tìm category 1 lần
        verify(categoryRepository, times(1)).saveAndFlush(any(Category.class)); // Lưu 1 lần
    }

    /**
     * Test case 6: Kiểm tra cập nhật category không tồn tại
     * Kịch bản: Cập nhật category với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException, không thực hiện lưu
     */
    @Test
    @DisplayName("Test 6: Cập nhật category - Không tìm thấy")
    void testUpdateCategoryId_NotFound() {
        // ARRANGE - Tạo request cập nhật
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Test");
        request.setStatus(1);

        // Mock repository không tìm thấy category
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Kiểm tra exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategoryId(request, "invalid-id");
        });

        // Verify exception message và không có update nào được thực hiện
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id"); // Tìm 1 lần
        verify(categoryRepository, never()).saveAndFlush(any(Category.class)); // Không save
    }

    /**
     * Test case 7: Kiểm tra xóa category
     * Kịch bản: Xóa category có ID tồn tại
     * Kết quả mong đợi: Category được xóa khỏi database
     */
    @Test
    @DisplayName("Test 7: Xóa category - Thành công")
    void testDeleteCategoryId_Success() {
        // ARRANGE - Mock repository tìm thấy và xóa category
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory); // Mock void method

        // ACT - Gọi service để xóa category
        categoryService.deleteCategoryId("cat-001");

        // ASSERT - Verify cả tìm kiếm và xóa đều được thực hiện
        verify(categoryRepository, times(1)).findById("cat-001"); // Tìm 1 lần
        verify(categoryRepository, times(1)).delete(testCategory); // Xóa 1 lần
    }

    /**
     * Test case 8: Kiểm tra xóa category không tồn tại
     * Kịch bản: Xóa category với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException, không thực hiện xóa
     */
    @Test
    @DisplayName("Test 8: Xóa category - Không tìm thấy")
    void testDeleteCategoryId_NotFound() {
        // ARRANGE - Mock repository không tìm thấy category
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Kiểm tra exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategoryId("invalid-id");
        });

        // Verify exception message và không có delete nào được thực hiện
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById("invalid-id"); // Tìm 1 lần
        verify(categoryRepository, never()).delete(any(Category.class)); // Không xóa
    }

    /**
     * Test case 9: Kiểm tra tìm kiếm category theo tên
     * Kịch bản: Tìm kiếm với keyword có kết quả
     * Kết quả mong đợi: Trả về Page chứa các category khớp với keyword
     */
    @Test
    @DisplayName("Test 9: Tìm kiếm category - Thành công")
    void testSearchCategory_Success() {
        // ARRANGE - Chuẩn bị keyword và kết quả tìm kiếm
        String keyword = "Trái cây";
        List<Category> categories = List.of(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        // Mock repository trả về page chứa categories tìm được
        when(categoryRepository.findByCategoryName(keyword, pageable)).thenReturn(categoryPage);

        // ACT - Gọi service để tìm kiếm
        Page<CategoryResponse> result = categoryService.searchCategory(keyword, pageable);

        // ASSERT - Verify kết quả tìm kiếm
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // Tìm thấy 1 kết quả
        verify(categoryRepository, times(1)).findByCategoryName(keyword, pageable); // Method được gọi đúng
    }

    /**
     * Test case 10: Kiểm tra tìm kiếm không có kết quả
     * Kịch bản: Tìm kiếm với keyword không khớp với category nào
     * Kết quả mong đợi: Trả về Page rỗng (empty page)
     */
    @Test
    @DisplayName("Test 10: Tìm kiếm category - Không có kết quả")
    void testSearchCategory_NoResults() {
        // ARRANGE - Chuẩn bị keyword không tồn tại và page rỗng
        String keyword = "NotExist";
        Page<Category> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        // Mock repository trả về empty page
        when(categoryRepository.findByCategoryName(keyword, pageable)).thenReturn(emptyPage);

        // ACT - Gọi service để tìm kiếm
        Page<CategoryResponse> result = categoryService.searchCategory(keyword, pageable);

        // ASSERT - Verify trả về page rỗng
        assertNotNull(result); // Kết quả không null
        assertEquals(0, result.getTotalElements()); // Không có phần tử nào
        assertTrue(result.getContent().isEmpty()); // Content rỗng
        verify(categoryRepository, times(1)).findByCategoryName(keyword, pageable); // Method được gọi
    }
}
