package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Category.CreateCategoryRequest;
import server.FruitShop.dto.request.Category.UpdateCategoryRequest;
import server.FruitShop.entity.Category;
import server.FruitShop.repository.CategoryRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Category API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional // Rollback sau mỗi test
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu cũ
        categoryRepository.deleteAll();

        // Tạo category test
        testCategory = new Category();
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("Integration Test 1: Lấy tất cả category - Thành công")
    void testGetAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/category")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].categoryName").value("Trái cây nhiệt đới"));
    }

    @Test
    @DisplayName("Integration Test 2: Lấy category theo ID - Thành công")
    void testGetCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/category/{id}", testCategory.getCategoryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhiệt đới"))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    @DisplayName("Integration Test 3: Lấy category theo ID - Không tồn tại")
    void testGetCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/category/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test 4: Tạo category mới - Thành công")
    void testCreateCategory_Success() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setCategoryName("Trái cây nhập khẩu");
        request.setStatus(1);

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhập khẩu"))
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        long count = categoryRepository.count();
        assert count == 2; // 1 category ban đầu + 1 category mới
    }

    @Test
    @DisplayName("Integration Test 5: Cập nhật category - Thành công")
    void testUpdateCategory_Success() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Trái cây nhiệt đới cao cấp");
        request.setStatus(1);

        mockMvc.perform(put("/api/category/{id}", testCategory.getCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhiệt đới cao cấp"));

        // Verify trong database
        Category updated = categoryRepository.findById(testCategory.getCategoryId()).orElseThrow();
        assert updated.getCategoryName().equals("Trái cây nhiệt đới cao cấp");
    }

    @Test
    @DisplayName("Integration Test 6: Xóa category - Thành công")
    void testDeleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/category/{id}", testCategory.getCategoryId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = categoryRepository.existsById(testCategory.getCategoryId());
        assert !exists;
    }

    @Test
    @DisplayName("Integration Test 7: Tìm kiếm category theo tên - Thành công")
    void testSearchCategory_Success() throws Exception {
        // Tạo thêm category
        Category category2 = new Category();
        category2.setCategoryName("Trái cây ôn đới");
        category2.setStatus(1);
        categoryRepository.save(category2);

        mockMvc.perform(get("/api/category/search")
                        .param("keyword", "nhiệt đới")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName", containsString("nhiệt đới")));
    }

    @Test
    @DisplayName("Integration Test 8: Tìm kiếm category - Không tìm thấy")
    void testSearchCategory_NotFound() throws Exception {
        mockMvc.perform(get("/api/category/search")
                        .param("keyword", "không tồn tại")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Integration Test 9: Tạo category - Thiếu dữ liệu bắt buộc")
    void testCreateCategory_MissingRequiredFields() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        // Không set categoryName (required field)
        request.setStatus(1);

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integration Test 10: Cập nhật category - Không tồn tại")
    void testUpdateCategory_NotFound() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setCategoryName("Test");
        request.setStatus(1);

        mockMvc.perform(put("/api/category/{id}", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
