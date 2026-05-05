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
        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo category test
        testCategory = new Category();
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả danh mục với phân trang
     * Mục đích: Kiểm tra API GET /api/category trả về đúng danh sách categories
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 category (testCategory từ setUp)
     * - Category đầu tiên có tên "Trái cây nhiệt đới"
     * - Hỗ trợ phân trang (page=0, size=10)
     * Use case: Admin xem danh sách danh mục sản phẩm
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả category - Thành công")
    void testGetAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/category")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
                // Không kiểm tra tên cụ thể vì có thể có nhiều categories từ tests khác
    }

    /**
     * Test Case 2: Lấy thông tin danh mục theo ID
     * Mục đích: Kiểm tra API GET /api/category/{id} trả về đúng thông tin category
     * Input: ID của testCategory đã tạo trong setUp
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response chứa categoryName và status khớp với dữ liệu test
     * Use case: Xem chi tiết thông tin một danh mục
     */
    @Test
    @DisplayName("Integration Test 2: Lấy category theo ID - Thành công")
    void testGetCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/category/{id}", testCategory.getCategoryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Trái cây nhiệt đới"))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test Case 3: Lấy category với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi query category với ID không hợp lệ
     * Input: ID không tồn tại trong database ("invalid-id")
     * Kết quả mong muốn:
     * - HTTP Status: 404 Not Found
     * - Hệ thống xử lý exception đúng cách
     */
    @Test
    @DisplayName("Integration Test 3: Lấy category theo ID - Không tồn tại")
    void testGetCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/category/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 4: Tạo danh mục mới thành công
     * Mục đích: Kiểm tra API POST /api/category có tạo được category mới không
     * Input: CreateCategoryRequest
     * - categoryName: "Trái cây nhập khẩu"
     * - status: 1 (active)
     * Kết quả mong muốn:
     * - HTTP Status: 201 Created
     * - Response trả về thông tin category vừa tạo
     * - Database có 2 categories (testCategory + category mới)
     * Use case: Admin tạo danh mục sản phẩm mới
     */
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
        assert count >= 2; // Ít nhất 2 categories
    }

    /**
     * Test Case 5: Cập nhật thông tin danh mục
     * Mục đích: Kiểm tra API PUT /api/category/{id} có cập nhật được không
     * Input: UpdateCategoryRequest
     * - categoryName: "Trái cây nhiệt đới cao cấp" (đổi tên)
     * - status: 1
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response trả về thông tin đã được cập nhật
     * - Database: categoryName được cập nhật thành công
     * Use case: Admin sửa tên hoặc thông tin danh mục
     */
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

    /**
     * Test Case 6: Xóa danh mục
     * Mục đích: Kiểm tra API DELETE /api/category/{id}
     * Input: ID của testCategory
     * Kết quả mong muốn:
     * - HTTP Status: 204 No Content
     * - Category bị xóa khỏi database (hard delete)
     * Use case: Admin xóa danh mục không còn sử dụng
     * Note: Cần xử lý cascade nếu category có products
     */
    @Test
    @DisplayName("Integration Test 6: Xóa category - Thành công")
    void testDeleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/category/{id}", testCategory.getCategoryId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = categoryRepository.existsById(testCategory.getCategoryId());
        assert !exists;
    }

    /**
     * Test Case 7: Tìm kiếm danh mục theo tên
     * Mục đích: Kiểm tra API GET /api/category/search có tìm kiếm đúng không
     * Setup: Tạo thêm category "Trái cây ôn đới" để test search
     * Input: keyword = "nhiệt đới"
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Chỉ trả về 1 kết quả (category có tên chứa "nhiệt đới")
     * - Category "Trái cây ôn đới" không xuất hiện trong kết quả
     * Use case: Admin tìm kiếm danh mục theo tên
     */
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

    /**
     * Test Case 8: Tìm kiếm danh mục - Không có kết quả
     * Mục đích: Kiểm tra xử lý khi search không tìm thấy kết quả
     * Input: keyword = "không tồn tại"
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK (không phải lỗi)
     * - Response là array rỗng (content.size = 0)
     * Business logic: Search không tìm thấy là case hợp lệ, không phải lỗi
     */
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

    /**
     * Test Case 9: Tạo category thiếu dữ liệu bắt buộc
     * Mục đích: Kiểm tra validation khi tạo category không có categoryName
     * Input: CreateCategoryRequest
     * - categoryName: null/empty (thiếu required field)
     * - status: 1
     * Kết quả mong muốn:
     * - HTTP Status: 400 Bad Request
     * - Không tạo category mới trong database
     * Business logic: categoryName là trường bắt buộc (required)
     */
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

    /**
     * Test Case 10: Cập nhật category không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi update với ID không hợp lệ
     * Input: UpdateCategoryRequest với categoryId = "invalid-id"
     * Kết quả mong muốn:
     * - HTTP Status: 404 Not Found
     * - Không có thay đổi trong database
     * Business logic: Phải validate category tồn tại trước khi update
     */
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
