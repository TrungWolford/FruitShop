package fruitshop.order_service.config;

import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.event.PaymentCompletedEvent;
import fruitshop.order_service.event.PaymentFailedEvent;
import fruitshop.order_service.event.RefundCompletedEvent;
import fruitshop.order_service.event.RefundFailedEvent;
import fruitshop.order_service.event.AccountDeactivatedEvent;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.RefundRepository;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventConfig {

    private final OrderRepository orderRepository;
    private final ShippingService shippingService;
    private final RefundRepository refundRepository;


    @Bean
    public Consumer<PaymentCompletedEvent> paymentCompletedConsumer() {
        return event -> {
            log.info("Received PaymentCompletedEvent for order: {}", event.getOrderId());
            orderRepository.findById(event.getOrderId()).ifPresent(order -> {
                order.setStatus(2); // 2 = PAYMENT_COMPLETED
                orderRepository.save(order);
                log.info("Order {} status updated to PAYMENT_COMPLETED(2)", order.getOrderId());
                
                // Automatically create initial Shipping record
                try {
                    Shipping sh = new Shipping();
                    sh.setAccountId(order.getAccountId());
                    sh.setStatus(0); // 0 = PENDING/CREATED
                    shippingService.upsert(order.getOrderId(), sh);
                    log.info("Created initial pending shipping record for Order: {}", order.getOrderId());
                } catch (Exception e) {
                    log.error("Failed to default-init shipping record for Order: {}", order.getOrderId(), e);
                }
            });
        };
    }

    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedConsumer() {
        return event -> {
            log.info("Received PaymentFailedEvent for order: {}", event.getOrderId());
            orderRepository.findById(event.getOrderId()).ifPresent(order -> {
                order.setStatus(9); // 9 = CANCELLED
                orderRepository.save(order);
                log.info("Order {} status updated to CANCELLED(9)", order.getOrderId());
            });
        };
    }

    @Bean
    public Consumer<RefundCompletedEvent> refundCompletedConsumer() {
        return event -> {
            log.info("Received RefundCompletedEvent for refund: {}", event.getRefundId());
            refundRepository.findById(event.getRefundId()).ifPresent(refund -> {
                refund.setRefundStatus("COMPLETED");
                refund.setProcessedAt(Instant.now());
                refundRepository.save(refund);
                log.info("Refund {} status updated to COMPLETED", refund.getRefundId());
            });
        };
    }

    @Bean
    public Consumer<RefundFailedEvent> refundFailedConsumer() {
        return event -> {
            log.info("Received RefundFailedEvent for refund: {} with reason: {}", event.getRefundId(), event.getReason());
            refundRepository.findById(event.getRefundId()).ifPresent(refund -> {
                refund.setRefundStatus("FAILED");
                refund.setProcessedAt(Instant.now());
                refundRepository.save(refund);
                log.info("Refund {} status updated to FAILED", refund.getRefundId());
            });
        };
    }

    @Bean
    public Consumer<AccountDeactivatedEvent> accountDeactivatedConsumer() {
        return event -> {
            log.info("Received AccountDeactivatedEvent for account: {}", event.getAccountId());
            try {
                // Archive logic or cancel pending orders
                orderRepository.findByAccountId(event.getAccountId()).forEach(order -> {
                    if (order.getStatus() == 0 || order.getStatus() == 1) { // 0 = PENDING, 1 = CONFIRMED
                        order.setStatus(3); // 3 = CANCELLED
                        orderRepository.save(order);
                        log.info("Cancelled order: {} due to account deactivation", order.getOrderId());
                    }
                });
                log.info("Successfully processed AccountDeactivatedEvent for account: {}", event.getAccountId());
            } catch (Exception e) {
                log.error("Error processing AccountDeactivatedEvent for account: {}", event.getAccountId(), e);
            }
        };
    }
}
