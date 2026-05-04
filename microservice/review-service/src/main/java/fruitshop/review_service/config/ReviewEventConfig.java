package fruitshop.review_service.config;

import fruitshop.review_service.event.ProductDeletedEvent;
import fruitshop.review_service.event.AccountDeactivatedEvent;
import fruitshop.review_service.entity.Rating;
import fruitshop.review_service.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConfig {

    private final RatingRepository ratingRepository;

    @Bean
    public Consumer<ProductDeletedEvent> productDeletedConsumer() {
        return event -> {
            log.info("Received ProductDeletedEvent for product: {}. Soft deleting reviews...", event.getProductId());
            try {
                List<Rating> ratings = ratingRepository.findByProductId(event.getProductId());
                for (Rating r : ratings) {
                    r.setStatus(0); // 0 = Inactive / Soft deleted
                }
                ratingRepository.saveAll(ratings);
                log.info("Successfully soft deleted {} reviews for product: {}", ratings.size(), event.getProductId());
            } catch (Exception e) {
                log.error("Failed to process soft-delete reviews for product: {}", event.getProductId(), e);
            }
        };
    }

    @Bean
    public Consumer<AccountDeactivatedEvent> accountDeactivatedConsumer() {
        return event -> {
            log.info("Received AccountDeactivatedEvent for account: {}. Soft deleting reviews...", event.getAccountId());
            try {
                List<Rating> ratings = ratingRepository.findByAccountId(event.getAccountId());
                for (Rating r : ratings) {
                    r.setStatus(0); // 0 = Inactive / Soft deleted
                }
                ratingRepository.saveAll(ratings);
                log.info("Successfully soft deleted {} reviews for account: {}", ratings.size(), event.getAccountId());
            } catch (Exception e) {
                log.error("Failed to process soft-delete reviews for account: {}", event.getAccountId(), e);
            }
        };
    }
}
