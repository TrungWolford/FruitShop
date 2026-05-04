package fruitshop.catalog_service.config;

import fruitshop.catalog_service.event.RatingCreatedEvent;
import fruitshop.catalog_service.event.RatingUpdatedEvent;
import fruitshop.catalog_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CatalogEventConfig {

    private final ProductService productService;

    @Bean
    public Consumer<RatingCreatedEvent> ratingCreatedConsumer() {
        return event -> {
            log.info("Received RatingCreatedEvent for product: {}", event.getProductId());
            updateProductRating(event.getProductId());
        };
    }

    @Bean
    public Consumer<RatingUpdatedEvent> ratingUpdatedConsumer() {
        return event -> {
            log.info("Received RatingUpdatedEvent for product: {}", event.getProductId());
            updateProductRating(event.getProductId());
        };
    }

    private void updateProductRating(String productId) {
        try {
            // Note: Catalog recalculates logic by itself or relies on Review Service
            // Either Review Service recalculates and publishes ProductRatingUpdated
            // Or Catalog Service calls Review Service to get the updated rating
            // The Saga doc says "Catalog Service Listens -> Query all ratings -> Recalculate -> Update product"
            // Wait, we need to call Review Service feign client to get the new average star?
            productService.updateAverageRatingFromReviewService(productId);
            log.info("Successfully updated average rating for product: {}", productId);
        } catch (Exception e) {
            log.error("Failed to update average rating for product: {}", productId, e);
        }
    }
}
