package fruitshop.payment_service.config;

import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.event.OrderCreatedEvent;
import fruitshop.payment_service.event.RefundApprovedEvent;
import fruitshop.payment_service.event.RefundCompletedEvent;
import fruitshop.payment_service.event.RefundFailedEvent;
import fruitshop.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConfig {

    private final PaymentService paymentService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());
            try {
                PaymentRequest request = new PaymentRequest();
                request.setOrderId(event.getOrderId());
                request.setAmount(BigDecimal.valueOf(event.getTotalAmount()));
                request.setPaymentStatus(0); // 0 = PENDING
                request.setPaymentMethod("UNSELECTED");
                request.setPaymentDate(new Date());
                paymentService.createPayment(request);
                log.info("Successfully created pending payment for order: {}", event.getOrderId());
            } catch (Exception e) {
                log.error("Error creating payment for order: {}", event.getOrderId(), e);
            }
        };
    }

    @Bean
    public Consumer<RefundApprovedEvent> refundApprovedConsumer() {
        return event -> {
            log.info("Received RefundApprovedEvent for refund: {}, order: {}", event.getRefundId(), event.getOrderId());
            try {
                // Simulate Refund processing (e.g., calling Stripe/VNPAY Refund API)
                log.info("Simulating gateway refund process for amount: {}", event.getAmount());
                
                String transactionId = "REF-" + UUID.randomUUID().toString();
                
                RefundCompletedEvent completedEvent = new RefundCompletedEvent(
                    event.getRefundId(),
                    event.getOrderId(),
                    transactionId
                );
                
                streamBridge.send("refundCompletedSupplier-out-0", completedEvent);
                log.info("Successfully processed refund {} and published RefundCompletedEvent", event.getRefundId());

            } catch (Exception e) {
                log.error("Failed to process refund: {}", event.getRefundId(), e);
                
                RefundFailedEvent failedEvent = new RefundFailedEvent(
                    event.getRefundId(),
                    event.getOrderId(),
                    e.getMessage() != null ? e.getMessage() : "Unknown gateway error"
                );
                streamBridge.send("refundFailedSupplier-out-0", failedEvent);
            }
        };
    }
}
