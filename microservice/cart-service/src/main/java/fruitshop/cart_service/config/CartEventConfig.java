package fruitshop.cart_service.config;

import fruitshop.cart_service.event.OrderCreatedEvent;
import fruitshop.cart_service.event.ProductDeletedEvent;
import fruitshop.cart_service.event.ProductUpdatedEvent;
import fruitshop.cart_service.event.AccountDeactivatedEvent;
import fruitshop.cart_service.repository.CartItemRepository;
import fruitshop.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CartEventConfig {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            log.info("Received OrderCreatedEvent for order: {}. Clearing cart for account: {}",
                    event.getOrderId(), event.getAccountId());
            try {
                cartService.clearCart(event.getAccountId());
                log.info("Successfully cleared cart for account: {}", event.getAccountId());
            } catch (Exception e) {
                log.error("Failed to clear cart for account: {}", event.getAccountId(), e);
            }
        };
    }

    @Bean
    public Consumer<ProductDeletedEvent> productDeletedConsumer() {
        return event -> {
            log.info("Received ProductDeletedEvent for product: {}. Removing from all carts...", event.getProductId());
            try {
                cartItemRepository.deleteByProductId(event.getProductId());
                log.info("Successfully removed product {} from all carts", event.getProductId());
            } catch (Exception e) {
                log.error("Failed to remove product from carts: {}", event.getProductId(), e);
            }
        };
    }

    @Bean
    public Consumer<ProductUpdatedEvent> productUpdatedConsumer() {
        return event -> {
            log.info("Received ProductUpdatedEvent for product: {}. Updating constraints...", event.getProductId());
            try {
                // In cart logic we can just delete if it violates, or we just notify. 
                // Or we update prices if pricing is duplicated in carts.
                // Assuming we might update product prices cached in the cart item
                log.info("Handled update event effectively (Placeholder).");
            } catch (Exception e) {
                log.error("Failed to handle update for product: {}", event.getProductId(), e);
            }
        };
    }

    @Bean
    public Consumer<AccountDeactivatedEvent> accountDeactivatedConsumer() {
        return event -> {
            log.info("Received AccountDeactivatedEvent for account: {}. Clearing cart...", event.getAccountId());
            try {
                cartService.clearCart(event.getAccountId());
                log.info("Successfully cleared cart for deactivated account: {}", event.getAccountId());
            } catch (Exception e) {
                log.error("Error processing AccountDeactivatedEvent for account: {}", event.getAccountId(), e);
            }
        };
    }
}
