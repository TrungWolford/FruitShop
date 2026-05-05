package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.FruitShop.dto.request.Rating.CreateRatingRequest;
import server.FruitShop.dto.request.Rating.UpdateRatingRequest;
import server.FruitShop.dto.response.Rating.RatingResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.Rating;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.OrderItemRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.repository.RatingRepository;
import server.FruitShop.service.Impl.RatingServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho RatingService
 * Class này test tất cả các chức năng liên quan đến đánh giá sản phẩm
 * bao gồm: tạo, cập nhật, thay đổi trạng thái và tính điểm trung bình
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Rating Service")
class RatingServiceImplTest {

    @Mock // Mock repository quản lý đánh giá
    private RatingRepository ratingRepository;

    @Mock // Mock repository quản lý tài khoản
    private AccountRepository accountRepository;

    @Mock // Mock repository quản lý sản phẩm
    private ProductRepository productRepository;

    @Mock // Mock repository quản lý order items
    private OrderItemRepository orderItemRepository;

    @InjectMocks // Inject tất cả mock vào service
    private RatingServiceImpl ratingService;

    // Các entity mẫu để test
    private Rating testRating;
    private Account testAccount;
    private Product testProduct;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo account, product và rating mẫu với đánh giá 5 sao
     */
    @BeforeEach
    void setUp() {
        // Tạo account test
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Nguyễn Văn A");

        // Tạo product test
        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài Úc");

        // Tạo rating test với 5 sao
        testRating = new Rating();
        testRating.setRatingId("rating-001");
        testRating.setAccount(testAccount); // Người đánh giá
        testRating.setProduct(testProduct); // Sản phẩm được đánh giá
        testRating.setRatingStar(5); // 5 sao
        testRating.setComment("Sản phẩm rất tốt!"); // Nhận xét
        testRating.setStatus(1); // 1 = Hiển thị
    }

    /**
     * Test case 1: Kiểm tra tạo rating mới
     * Kịch bản: Khách hàng đánh giá sản phẩm sau khi mua
     * Kết quả mong đợi: Rating được tạo và lưu vào database
     */
    @Test
    @DisplayName("Test 1: Tạo rating - Thành công")
    void testCreateRating_Success() {
        // ARRANGE - Tạo request với thông tin đánh giá
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("acc-001"); // Người đánh giá
        request.setProductId("prod-001"); // Sản phẩm được đánh giá
        request.setRatingStar(5); // 5 sao
        request.setComment("Tuyệt vời!"); // Nhận xét

        // Mock repository tìm account và product, sau đó lưu rating
        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        // ACT - Tạo rating
        RatingResponse result = ratingService.createRating(request);

        // ASSERT - Verify rating được tạo thành công
        assertNotNull(result);
        verify(accountRepository, times(1)).findById("acc-001"); // Verify account tồn tại
        verify(productRepository, times(1)).findById("prod-001"); // Verify product tồn tại
        verify(ratingRepository, times(1)).save(any(Rating.class)); // Lưu rating
    }

    /**
     * Test case 2: Kiểm tra tạo rating với account không tồn tại
     * Kịch bản: Cố gắng tạo rating với account ID không hợp lệ
     * Kết quả mong đợi: Throw RuntimeException, không lưu rating
     */
    @Test
    @DisplayName("Test 2: Tạo rating - Account không tồn tại")
    void testCreateRating_AccountNotFound() {
        // ARRANGE - Request với account ID không tồn tại
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("invalid-id");
        request.setProductId("prod-001");
        request.setRatingStar(5);

        // Mock repository không tìm thấy account
        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        assertThrows(RuntimeException.class, () -> {
            ratingService.createRating(request);
        });
        
        // Verify không có rating nào được lưu
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    /**
     * Test case 3: Kiểm tra tạo rating với product không tồn tại
     * Kịch bản: Cố gắng tạo rating với product ID không hợp lệ
     * Kết quả mong đợi: Throw RuntimeException, không lưu rating
     */
    @Test
    @DisplayName("Test 3: Tạo rating - Product không tồn tại")
    void testCreateRating_ProductNotFound() {
        // ARRANGE - Request với product ID không tồn tại
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("acc-001");
        request.setProductId("invalid-id"); // Product không tồn tại
        request.setRatingStar(5);

        // Mock repository tìm thấy account nhưng không tìm thấy product
        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        assertThrows(RuntimeException.class, () -> {
            ratingService.createRating(request);
        });
        
        // Verify không có rating nào được lưu
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    /**
     * Test case 4: Kiểm tra cập nhật rating
     * Kịch bản: Khách hàng chỉnh sửa đánh giá đã tạo trước đó
     * Kết quả mong đợi: Rating được cập nhật với thông tin mới
     */
    @Test
    @DisplayName("Test 4: Cập nhật rating - Thành công")
    void testUpdateRating_Success() {
        // ARRANGE - Tạo request với thông tin cập nhật
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(4); // Giảm xuống 4 sao
        request.setComment("Rất tốt"); // Comment mới
        request.setStatus(1); // Vẫn hiển thị

        // Mock repository tìm và lưu rating
        when(ratingRepository.findById("rating-001")).thenReturn(Optional.of(testRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        // ACT - Cập nhật rating
        RatingResponse result = ratingService.updateRating(request, "rating-001");

        // ASSERT - Verify rating được cập nhật
        assertNotNull(result);
        verify(ratingRepository, times(1)).findById("rating-001");
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    /**
     * Test case 5: Kiểm tra cập nhật rating không tồn tại
     * Kịch bản: Cố gắng cập nhật rating với ID không hợp lệ
     * Kết quả mong đợi: Throw RuntimeException, không thực hiện cập nhật
     */
    @Test
    @DisplayName("Test 5: Cập nhật rating - Không tìm thấy")
    void testUpdateRating_NotFound() {
        // ARRANGE - Request cập nhật
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(4);
        request.setStatus(1);

        // Mock repository không tìm thấy rating
        when(ratingRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        assertThrows(RuntimeException.class, () -> {
            ratingService.updateRating(request, "invalid-id");
        });
        
        // Verify không có update nào được thực hiện
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    /**
     * Test case 6: Kiểm tra thay đổi trạng thái hiển thị rating
     * Kịch bản: Admin ẩn/hiện rating (toggle status giữa 0 và 1)
     * Kết quả mong đợi: Status của rating được chuyển đổi
     */
    @Test
    @DisplayName("Test 6: Thay đổi status rating - Thành công")
    void testChangeStatus_Success() {
        // ARRANGE - Mock repository tìm và lưu rating
        when(ratingRepository.findById("rating-001")).thenReturn(Optional.of(testRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        // ACT - Toggle status (1 -> 0 hoặc 0 -> 1)
        RatingResponse result = ratingService.changeStatus("rating-001");

        // ASSERT - Verify status được thay đổi
        assertNotNull(result);
        verify(ratingRepository, times(1)).findById("rating-001");
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    /**
     * Test case 7: Kiểm tra tính điểm rating trung bình
     * Kịch bản: Tính trung bình rating của product, chỉ tính ratings có status = 1
     * Kết quả mong đợi: Trả về trung bình của ratings đang hiển thị (5+4)/2 = 4.5
     */
    @Test
    @DisplayName("Test 7: Tính rating trung bình - Thành công")
    void testCalculateRatingStarByProductId_Success() {
        // ARRANGE - Tạo 3 ratings với status khác nhau
        Rating rating1 = new Rating();
        rating1.setRatingStar(5); // 5 sao
        rating1.setStatus(1); // Hiển thị - tính vào

        Rating rating2 = new Rating();
        rating2.setRatingStar(4); // 4 sao
        rating2.setStatus(1); // Hiển thị - tính vào

        Rating rating3 = new Rating();
        rating3.setRatingStar(3); // 3 sao
        rating3.setStatus(0); // Ẩn - KHÔNG tính vào

        List<Rating> ratings = Arrays.asList(rating1, rating2, rating3);

        // Mock repository tìm product và ratings
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.findByProductProductId("prod-001")).thenReturn(ratings);

        // ACT - Tính rating trung bình
        double result = ratingService.calculateRatingStarByProductId("prod-001");

        // ASSERT - Verify kết quả: (5+4)/2 = 4.5 (chỉ tính rating1 và rating2)
        assertEquals(4.5, result, 0.01); // Chỉ tính rating1 (5) và rating2 (4), bỏ qua rating3 (status=0)
        verify(ratingRepository, times(1)).findByProductProductId("prod-001");
    }

    /**
     * Test case 8: Kiểm tra tính rating khi không có đánh giá
     * Kịch bản: Product chưa có rating nào
     * Kết quả mong đợi: Trả về 0.0
     */
    @Test
    @DisplayName("Test 8: Tính rating - Không có ratings")
    void testCalculateRatingStarByProductId_NoRatings() {
        // ARRANGE - Mock product tồn tại nhưng không có ratings
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.findByProductProductId("prod-001")).thenReturn(List.of()); // Danh sách rỗng

        // ACT - Tính rating trung bình
        double result = ratingService.calculateRatingStarByProductId("prod-001");

        // ASSERT - Verify trả về 0.0 khi không có ratings
        assertEquals(0.0, result);
        verify(ratingRepository, times(1)).findByProductProductId("prod-001");
    }

    /**
     * Test case 9: Kiểm tra tính rating với product không tồn tại
     * Kịch bản: Cố gắng tính rating cho product ID không hợp lệ
     * Kết quả mong đợi: Throw RuntimeException
     */
    @Test
    @DisplayName("Test 9: Tính rating - Product không tồn tại")
    void testCalculateRatingStarByProductId_ProductNotFound() {
        // ARRANGE - Mock repository không tìm thấy product
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        assertThrows(RuntimeException.class, () -> {
            ratingService.calculateRatingStarByProductId("invalid-id");
        });
    }
}
