package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Rating API
 * Rating Status: 0=Ẩn, 1=Hiển thị
 * Rating Star: 1-5 sao
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class RatingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;
    private Product testProduct;
    private Rating testRating;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo role
        Role customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");
        customerRole = roleRepository.save(customerRole);

        // Tạo account
        testAccount = new Account();
        testAccount.setAccountName("Nguyễn Văn A");
        testAccount.setAccountPhone("0355142890");
        testAccount.setPassword(passwordEncoder.encode("123456"));
        testAccount.setStatus(1);
        testAccount.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
        testAccount = accountRepository.save(testAccount);

        // Tạo category
        Category testCategory = new Category();
        testCategory.setCategoryName("Trái cây nhập khẩu");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);

        // Tạo product
        testProduct = new Product();
        testProduct.setProductName("Xoài Úc");
        testProduct.setPrice(50000);
        testProduct.setStock(100);
        testProduct.setDescription("Xoài nhập khẩu từ Úc");
        testProduct.setStatus(1);
        testProduct.setCategories(Collections.singletonList(testCategory));
        testProduct = productRepository.save(testProduct);

        // Tạo rating
        testRating = new Rating();
        testRating.setAccount(testAccount);
        testRating.setProduct(testProduct);
        testRating.setRatingStar(5);
        testRating.setComment("Xoài rất ngon!");
        testRating.setStatus(1);
        testRating = ratingRepository.save(testRating);
    }

    /**
     * Test 1: Lấy tất cả đánh giá
     * Mục đích: Kiểm tra API GET /api/rating lấy danh sách tất cả ratings
     * Input: page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả ratings - Thành công")
    void testGetAllRatings_Success() throws Exception {
        mockMvc.perform(get("/api/rating")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].ratingStar").value(5));
    }

    /**
     * Test 2: Lấy đánh giá của một tài khoản
     * Mục đích: Kiểm tra API GET /api/rating/account/{accountId} lấy tất cả ratings của một người dùng
     * Input: accountId hợp lệ, page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 2: Lấy ratings theo accountId - Thành công")
    void testGetRatingsByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/rating/account/{accountId}", testAccount.getAccountId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].account.accountId").value(testAccount.getAccountId()));
    }

    /**
     * Test 3: Lấy đánh giá của một sản phẩm
     * Mục đích: Kiểm tra API GET /api/rating/product/{productId} lấy tất cả ratings của một sản phẩm
     * Input: productId hợp lệ, page=0, size=10
     */
    @Test
    @DisplayName("Integration Test 3: Lấy ratings theo productId - Thành công")
    void testGetRatingsByProductId_Success() throws Exception {
        mockMvc.perform(get("/api/rating/product/{productId}", testProduct.getProductId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].product.productId").value(testProduct.getProductId()));
    }

    /**
     * Test 4: Lấy đánh giá của một người dùng cho một sản phẩm cụ thể
     * Mục đích: Kiểm tra API GET /api/rating/account/{accountId}/product/{productId}
     * Input: accountId và productId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 4: Lấy ratings theo accountId và productId - Thành công")
    void testGetRatingsByAccountAndProduct_Success() throws Exception {
        mockMvc.perform(get("/api/rating/account/{accountId}/product/{productId}",
                        testAccount.getAccountId(), testProduct.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].ratingStar").value(5));
    }

    /**
     * Test 5: Tạo đánh giá mới
     * Mục đích: Kiểm tra API POST /api/rating tạo rating mới vào database
     * Input: CreateRatingRequest (accountId, productId, ratingStar=4, comment)
     */
    @Test
    @DisplayName("Integration Test 5: Tạo rating mới - Thành công")
    void testCreateRating_Success() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId(testAccount.getAccountId());
        request.setProductId(testProduct.getProductId());
        request.setRatingStar(4);
        request.setComment("Sản phẩm tốt");

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingStar").value(4))
                .andExpect(jsonPath("$.comment").value("Sản phẩm tốt"));

        // Verify trong database
        long count = ratingRepository.count();
        assert count == 2;
    }

    /**
     * Test 6: Cập nhật đánh giá
     * Mục đích: Kiểm tra API PUT /api/rating/{ratingId} cập nhật thông tin rating
     * Input: UpdateRatingRequest (ratingStar=3, comment, status=1)
     */
    @Test
    @DisplayName("Integration Test 6: Cập nhật rating - Thành công")
    void testUpdateRating_Success() throws Exception {
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(3);
        request.setComment("Sản phẩm bình thường");
        request.setStatus(1);

        mockMvc.perform(put("/api/rating/{ratingId}", testRating.getRatingId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingStar").value(3))
                .andExpect(jsonPath("$.comment").value("Sản phẩm bình thường"));

        // Verify trong database
        Rating updated = ratingRepository.findById(testRating.getRatingId()).orElseThrow();
        assert updated.getRatingStar() == 3;
    }

    /**
     * Test 7: Ẩn/hiện đánh giá
     * Mục đích: Kiểm tra API PATCH /api/rating/{ratingId}/status đổi trạng thái rating (1→0 hoặc 0→1)
     * Input: ratingId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 7: Thay đổi status rating - Thành công")
    void testChangeRatingStatus_Success() throws Exception {
        mockMvc.perform(patch("/api/rating/{ratingId}/status", testRating.getRatingId()))
                .andExpect(status().isOk());

        // Verify trong database
        Rating updated = ratingRepository.findById(testRating.getRatingId()).orElseThrow();
        assert updated.getStatus() == 0; // Status đã đổi từ 1 sang 0
    }

    /**
     * Test 8: Tính điểm trung bình đánh giá
     * Mục đích: Kiểm tra API GET /api/rating/product/{productId}/average tính rating trung bình của sản phẩm
     * Input: productId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 8: Tính average rating - Thành công")
    void testCalculateAverageRating_Success() throws Exception {
        mockMvc.perform(get("/api/rating/product/{productId}/average", testProduct.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(greaterThan(0.0)));
    }

    /**
     * Test 9: Tạo đánh giá với tài khoản không tồn tại
     * Mục đích: Kiểm tra API POST /api/rating trả lỗi 404 khi accountId không hợp lệ
     * Input: accountId không tồn tại, productId hợp lệ
     */
    @Test
    @DisplayName("Integration Test 9: Tạo rating - Account không tồn tại")
    void testCreateRating_AccountNotFound() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("invalid-account-id");
        request.setProductId(testProduct.getProductId());
        request.setRatingStar(5);
        request.setComment("Test");

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 10: Tạo đánh giá với sản phẩm không tồn tại
     * Mục đích: Kiểm tra API POST /api/rating trả lỗi 404 khi productId không hợp lệ
     * Input: accountId hợp lệ, productId không tồn tại
     */
    @Test
    @DisplayName("Integration Test 10: Tạo rating - Product không tồn tại")
    void testCreateRating_ProductNotFound() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId(testAccount.getAccountId());
        request.setProductId("invalid-product-id");
        request.setRatingStar(5);
        request.setComment("Test");

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
