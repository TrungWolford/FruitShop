package fruitshop.catalog_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.catalog_service.dto.request.Category.CreateCategoryRequest;
import fruitshop.catalog_service.dto.request.Category.UpdateCategoryRequest;
import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Category API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 */
class CategoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        
        // Tạo category test
        testCategory = new Category();
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả danh mục với phân trang
     * Mục đích: Kiểm tra API GET /api/catalog/category trả về đúng danh sách categories
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả category - Thành công")
    void testGetAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/catalog/categories/paginated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test Case 2: Lấy thông tin danh mục theo ID
     * Mục đích: Kiểm tra API GET /api/catalog/category/{id} trả về đúng thông tin category
     */
    @Test
    @DisplayName("Integration Test 2: Lấy category theo ID - Thành công")
    void testGetCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/catalog/categories/{id}", testCategory.getCategoryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhiệt đới"))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test Case 3: Lấy category với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi query category với ID không hợp lệ
     */
    @Test
    @DisplayName("Integration Test 3: Lấy category theo ID - Không tồn tại")
    void testGetCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/catalog/categories/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 4: Tạo danh mục mới thành công
     * Mục đích: Kiểm tra API POST /api/catalog/category có tạo được category mới không
     */
    @Test
    @DisplayName("Integration Test 4: Tạo category mới - Thành công")
    void testCreateCategory_Success() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setCategoryName("Trái cây nhập khẩu");
        request.setStatus(1);

        mockMvc.perform(post("/api/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // catalog-service trả về 200 thay vì 201
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhập khẩu"))
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        long count = categoryRepository.count();
        assert count >= 2;
    }

    /**
     * Test Case 5: Cập nhật thông tin danh mục
     * Mục đích: Kiểm tra API PUT /api/catalog/category/{id} có cập nhật được không
     */
    @Test
    @DisplayName("Integration Test 5: Cập nhật category - Thành công")
    void testUpdateCategory_Success() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Trái cây nhiệt đới cao cấp");
        request.setStatus(1);

        mockMvc.perform(put("/api/catalog/categories/{id}", testCategory.getCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhiệt đới cao cấp"));

        // Verify trong database
        Category updated = categoryRepository.findById(testCategory.getCategoryId()).orElseThrow();
        assert updated.getCategoryName().equals("Trái cây nhiệt đới cao cấp");
    }

    /**
     * Test Case 6: Xóa danh mục
     * Mục đích: Kiểm tra API DELETE /api/catalog/category/{id}
     */
    @Test
    @DisplayName("Integration Test 6: Xóa category - Thành công")
    void testDeleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/catalog/categories/{id}", testCategory.getCategoryId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = categoryRepository.existsById(testCategory.getCategoryId());
        assert !exists;
    }

    /**
     * Test Case 7: Tìm kiếm danh mục theo tên
     * Mục đích: Kiểm tra API GET /api/catalog/category/search có tìm kiếm đúng không
     */
    @Test
    @DisplayName("Integration Test 7: Tìm kiếm category theo tên - Thành công")
    void testSearchCategory_Success() throws Exception {
        // Tạo thêm category
        Category category2 = new Category();
        category2.setCategoryName("Trái cây ôn đới");
        category2.setStatus(1);
        categoryRepository.save(category2);

        mockMvc.perform(get("/api/catalog/categories/search")
                        .param("keyword", "nhiệt đới")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName", containsString("nhiệt đới")));
    }
}
