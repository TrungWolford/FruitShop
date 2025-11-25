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

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Rating Service")
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Rating testRating;
    private Account testAccount;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Nguyễn Văn A");

        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài Úc");

        testRating = new Rating();
        testRating.setRatingId("rating-001");
        testRating.setAccount(testAccount);
        testRating.setProduct(testProduct);
        testRating.setRatingStar(5);
        testRating.setComment("Sản phẩm rất tốt!");
        testRating.setStatus(1);
    }

    @Test
    @DisplayName("Test 1: Tạo rating - Thành công")
    void testCreateRating_Success() {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("acc-001");
        request.setProductId("prod-001");
        request.setRatingStar(5);
        request.setComment("Tuyệt vời!");

        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        RatingResponse result = ratingService.createRating(request);

        assertNotNull(result);
        verify(accountRepository, times(1)).findById("acc-001");
        verify(productRepository, times(1)).findById("prod-001");
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 2: Tạo rating - Account không tồn tại")
    void testCreateRating_AccountNotFound() {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("invalid-id");
        request.setProductId("prod-001");
        request.setRatingStar(5);

        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ratingService.createRating(request);
        });
        
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 3: Tạo rating - Product không tồn tại")
    void testCreateRating_ProductNotFound() {
        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("acc-001");
        request.setProductId("invalid-id");
        request.setRatingStar(5);

        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ratingService.createRating(request);
        });
        
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 4: Cập nhật rating - Thành công")
    void testUpdateRating_Success() {
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(4);
        request.setComment("Rất tốt");
        request.setStatus(1);

        when(ratingRepository.findById("rating-001")).thenReturn(Optional.of(testRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        RatingResponse result = ratingService.updateRating(request, "rating-001");

        assertNotNull(result);
        verify(ratingRepository, times(1)).findById("rating-001");
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 5: Cập nhật rating - Không tìm thấy")
    void testUpdateRating_NotFound() {
        UpdateRatingRequest request = new UpdateRatingRequest();
        request.setRatingStar(4);
        request.setStatus(1);

        when(ratingRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ratingService.updateRating(request, "invalid-id");
        });
        
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 6: Thay đổi status rating - Thành công")
    void testChangeStatus_Success() {
        when(ratingRepository.findById("rating-001")).thenReturn(Optional.of(testRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        RatingResponse result = ratingService.changeStatus("rating-001");

        assertNotNull(result);
        verify(ratingRepository, times(1)).findById("rating-001");
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    @DisplayName("Test 7: Tính rating trung bình - Thành công")
    void testCalculateRatingStarByProductId_Success() {
        Rating rating1 = new Rating();
        rating1.setRatingStar(5);
        rating1.setStatus(1);

        Rating rating2 = new Rating();
        rating2.setRatingStar(4);
        rating2.setStatus(1);

        Rating rating3 = new Rating();
        rating3.setRatingStar(3);
        rating3.setStatus(0); // Inactive

        List<Rating> ratings = Arrays.asList(rating1, rating2, rating3);

        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.findByProductProductId("prod-001")).thenReturn(ratings);

        double result = ratingService.calculateRatingStarByProductId("prod-001");

        assertEquals(4.5, result, 0.01); // Chỉ tính rating1 và rating2 (status=1)
        verify(ratingRepository, times(1)).findByProductProductId("prod-001");
    }

    @Test
    @DisplayName("Test 8: Tính rating - Không có ratings")
    void testCalculateRatingStarByProductId_NoRatings() {
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(ratingRepository.findByProductProductId("prod-001")).thenReturn(List.of());

        double result = ratingService.calculateRatingStarByProductId("prod-001");

        assertEquals(0.0, result);
        verify(ratingRepository, times(1)).findByProductProductId("prod-001");
    }

    @Test
    @DisplayName("Test 9: Tính rating - Product không tồn tại")
    void testCalculateRatingStarByProductId_ProductNotFound() {
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ratingService.calculateRatingStarByProductId("invalid-id");
        });
    }
}
