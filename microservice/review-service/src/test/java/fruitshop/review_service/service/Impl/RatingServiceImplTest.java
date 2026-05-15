package fruitshop.review_service.service.Impl;

import fruitshop.review_service.dto.request.Rating.CreateRatingRequest;
import fruitshop.review_service.entity.Rating;
import fruitshop.review_service.feign.AccountClient;
import fruitshop.review_service.feign.ProductClient;
import fruitshop.review_service.feign.dto.AccountSummaryDto;
import fruitshop.review_service.feign.dto.ProductSummaryDto;
import fruitshop.review_service.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private AccountClient accountClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    void calculateRatingStarByProductId_returnsRoundedAverageOfActiveRatings() {
        ProductSummaryDto product = new ProductSummaryDto();
        product.setProductId("p-1");
        when(productClient.getProductById("p-1")).thenReturn(product);

        Rating r1 = new Rating();
        r1.setRatingStar(4.0);
        r1.setStatus(1);

        Rating r2 = new Rating();
        r2.setRatingStar(5.0);
        r2.setStatus(1);

        Rating r3 = new Rating();
        r3.setRatingStar(1.0);
        r3.setStatus(0);

        when(ratingRepository.findByProductId("p-1")).thenReturn(List.of(r1, r2, r3));

        double avg = ratingService.calculateRatingStarByProductId("p-1");

        assertEquals(4.5, avg);
    }

    @Test
    void createRating_duplicateOrderItem_throwsAndSkipsSave() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("a-1");
        when(accountClient.getAccountById("a-1")).thenReturn(account);

        ProductSummaryDto product = new ProductSummaryDto();
        product.setProductId("p-1");
        when(productClient.getProductById("p-1")).thenReturn(product);

        Rating existing = new Rating();
        existing.setOrderItemId("oi-1");
        when(ratingRepository.findByOrderItemId("oi-1")).thenReturn(Optional.of(existing));

        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("a-1");
        request.setProductId("p-1");
        request.setOrderItemId("oi-1");
        request.setRatingStar(5);

        assertThrows(RuntimeException.class, () -> ratingService.createRating(request));
        verify(ratingRepository, never()).save(any(Rating.class));
        verify(streamBridge, never()).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getRatingById_found_returnsData() {
        Rating rating = new Rating();
        rating.setRatingId("r-1");
        rating.setProductId("p-1");
        rating.setRatingStar(4.5);
        rating.setStatus(1);

        when(ratingRepository.findById("r-1")).thenReturn(Optional.of(rating));

        var response = ratingService.getRatingById("r-1");

        assertEquals("r-1", response.getRatingId());
        assertEquals(4.5, response.getRatingStar());
    }

    @Test
    void createRating_validRequest_savesRating() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("a-1");
        when(accountClient.getAccountById("a-1")).thenReturn(account);

        ProductSummaryDto product = new ProductSummaryDto();
        product.setProductId("p-1");
        when(productClient.getProductById("p-1")).thenReturn(product);

        when(ratingRepository.findByOrderItemId("oi-2")).thenReturn(Optional.empty());

        Rating saved = new Rating();
        saved.setRatingId("r-2");
        saved.setAccountId("a-1");
        saved.setProductId("p-1");
        saved.setRatingStar(5);
        saved.setStatus(1);

        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);

        CreateRatingRequest request = new CreateRatingRequest();
        request.setAccountId("a-1");
        request.setProductId("p-1");
        request.setOrderItemId("oi-2");
        request.setRatingStar(5);

        var response = ratingService.createRating(request);

        assertEquals("r-2", response.getRatingId());
        verify(ratingRepository).save(any(Rating.class));
    }
}
