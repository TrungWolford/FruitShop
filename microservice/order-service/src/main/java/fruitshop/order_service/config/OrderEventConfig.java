package fruitshop.order_service.config;

import fruitshop.order_service.event.*;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.service.OrderEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventConfig {

  private final OrderRepository orderRepository;
  private final OrderEventHandler orderEventHandler;

  @Bean
  public Consumer<PaymentCompletedEvent> paymentCompletedConsumer() {
    return event -> {
      log.info("Received PaymentCompletedEvent for order: {}", event.getOrderId());
      orderEventHandler.handlePaymentCompleted(event);
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
      orderEventHandler.handleRefundCompleted(event);
    };
  }

  @Bean
  public Consumer<RefundFailedEvent> refundFailedConsumer() {
    return event -> {
      log.info("Received RefundFailedEvent for refund: {} with reason: {}", event.getRefundId(), event.getReason());
      orderEventHandler.handleRefundFailed(event);
    };
  }

  @Bean
  public Consumer<AccountDeactivatedEvent> accountDeactivatedConsumer() {
    return event -> {
      log.info("Received AccountDeactivatedEvent for account: {}", event.getAccountId());
      try {
        orderRepository.findByAccountId(event.getAccountId()).forEach(order -> {
          if (order.getStatus() == 0 || order.getStatus() == 1) {
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
