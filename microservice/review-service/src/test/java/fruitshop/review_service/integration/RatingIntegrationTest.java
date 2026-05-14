package fruitshop.review_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.dto.request.Rating.UpdateRatingRequest;
import fruitshop.review_service.entity.Rating;
import fruitshop.review_service.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Rating API
 */
@WithMockUser(authorities = "ROLE_ADMIN")
class RatingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Rating testRating;
    private final String TEST_ACCOUNT_ID = UUID.randomUUID().toString();
    private final String TEST_PRODUCT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        wireMock.resetAll();

        // Stub AccountClient
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/account/" + TEST_ACCOUNT_ID))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "accountId": "%s",
                              "accountName": "Nguyễn Văn A",
                              "accountPhone": "0355142890",
                              "status": 1
                            }
                            """.formatted(TEST_ACCOUNT_ID))));

        // Stub ProductClient
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/catalog/products/" + TEST_PRODUCT_ID))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "productId": "%s",
                              "productName": "Xoài Úc",
                              "price": 50000,
                              "status": 1
                            }
                            """.formatted(TEST_PRODUCT_ID))));

        // Tạo rating test
        testRating = new Rating();
        testRating.setAccountId(TEST_ACCOUNT_ID);
        testRating.setProductId(TEST_PRODUCT_ID);
        testRating.setRatingStar(5);
        testRating.setComment("Sản phẩm tuyệt vời!");
        testRating.setStatus(1);
        testRating.setCreatedAt(LocalDateTime.now());
        testRating = ratingRepository.save(testRating);
    }

    /**
     * Test 1: Lấy tất cả ratings với phân trang
     * Mục đích: Kiểm tra API GET /api/rating trả về danh sách ratings
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả ratings - Thành công")
    void testGetAllRatings_Success() throws Exception {
        mockMvc.perform(get("/api/rating")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].comment").value("Sản phẩm tuyệt vời!"));
    }

    /**
     * Test 2: Lấy rating theo ID
     * Mục đích: Kiểm tra API GET /api/rating/{id} trả về thông tin chi tiết rating
     */
    @Test
    @DisplayName("Integration Test 2: Lấy rating theo ID - Thành công")
    void testGetRatingById_Success() throws Exception {
        mockMvc.perform(get("/api/rating/{id}", testRating.getRatingId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingStar").value(5))
                .andExpect(jsonPath("$.comment").value("Sản phẩm tuyệt vời!"));
    }

    /**
     * Test 3: Lấy rating với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi rating không tồn tại
     */
    @Test
    @DisplayName("Integration Test 3: Lấy rating theo ID - Không tồn tại")
    void testGetRatingById_NotFound() throws Exception {
        mockMvc.perform(get("/api/rating/{id}", "invalid-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4: Lấy danh sách rating theo sản phẩm
     * Mục đích: Kiểm tra API GET /api/rating/product/{productId}
     */
    @Test
    @DisplayName("Integration Test 4: Lấy ratings theo productId - Thành công")
    void testGetRatingsByProductId_Success() throws Exception {
        mockMvc.perform(get("/api/rating/product/{productId}", TEST_PRODUCT_ID)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].product.productId").value(TEST_PRODUCT_ID));
    }

    /**
     * Test 5: Tạo rating mới
     * Mục đích: Kiểm tra API POST /api/rating
     */
    @Test
    @DisplayName("Integration Test 5: Tạo rating mới - Thành công")
    void testCreateRating_Success() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId(TEST_ACCOUNT_ID);
        request.setProductId(TEST_PRODUCT_ID);
        request.setRatingStar(4);
        request.setComment("Rất ngon!");

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingStar").value(4))
                .andExpect(jsonPath("$.comment").value("Rất ngon!"));

        long count = ratingRepository.count();
        assert count == 2;
    }

    /**
     * Test 6: Tạo rating với accountId không tồn tại
     * Mục đích: Kiểm tra validate downstream (AccountService trả về 404)
     */
    @Test
    @DisplayName("Integration Test 6: Tạo rating - Account không tồn tại")
    void testCreateRating_AccountNotFound() throws Exception {
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/account/invalid-acc"))
                .willReturn(WireMock.aResponse().withStatus(404))
        );

        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("invalid-acc");
        request.setProductId(TEST_PRODUCT_ID);
        request.setRatingStar(5);
        request.setComment("Good");

        mockMvc.perform(post("/api/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // Feign client ném ResourceNotFoundException
    }

    /**
     * Test 7: Cập nhật rating
     * Mục đích: Kiểm tra API PUT /api/rating/{id}
     */
    @Test
    @DisplayName("Integration Test 7: Cập nhật rating - Thành công")
    void testUpdateRating_Success() throws Exception {
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(3);
        request.setComment("Cũng tạm được");
        request.setStatus(1);

        mockMvc.perform(put("/api/rating/{id}", testRating.getRatingId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingStar").value(3))
                .andExpect(jsonPath("$.comment").value("Cũng tạm được"));

        Rating updated = ratingRepository.findById(testRating.getRatingId()).orElseThrow();
        assert updated.getRatingStar() == 3;
        assert updated.getComment().equals("Cũng tạm được");
    }

    /**
     * Test 8: Xóa rating
     * Mục đích: Kiểm tra API DELETE /api/rating/{id}
     */
    @Test
    @DisplayName("Integration Test 8: Xóa rating - Thành công")
    void testDeleteRating_Success() throws Exception {
        mockMvc.perform(delete("/api/rating/{id}", testRating.getRatingId()))
                .andExpect(status().isNoContent());

        boolean exists = ratingRepository.existsById(testRating.getRatingId());
        assert !exists;
    }
}
