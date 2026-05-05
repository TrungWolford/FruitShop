package fruitshop.order_service.service;

import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.event.OrderConfirmedEvent;
import fruitshop.order_service.event.PaymentCompletedEvent;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {

  private final OrderRepository orderRepository;
  private final ShippingService shippingService;
  private final RefundRepository refundRepository;
  private final StreamBridge streamBridge;

  @Transactional
  public void handlePaymentCompleted(PaymentCompletedEvent event) {
    orderRepository.findById(event.getOrderId()).ifPresent(order -> {
      order.setStatus(2); // 2 = PAYMENT_COMPLETED
      order.setPaymentId(event.getPaymentId());
      orderRepository.save(order);
      log.info("Order {} status updated to PAYMENT_COMPLETED(2)", order.getOrderId());

      // Publish OrderConfirmedEvent for Catalog Service to update stock
      try {
        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
            order.getOrderId(),
            order.getAccountId(),
            order.getOrderItems().stream()
                .map(item -> new OrderConfirmedEvent.OrderItemDto(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList()),
            Instant.now());
        streamBridge.send("orderConfirmedSupplier-out-0", confirmedEvent);
        log.info("Published OrderConfirmedEvent for order: {}", order.getOrderId());
      } catch (Exception e) {
        log.error("Failed to publish OrderConfirmedEvent for order: {}", order.getOrderId(), e);
      }

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
  }

  @Transactional
  public void handleRefundCompleted(fruitshop.order_service.event.RefundCompletedEvent event) {
    refundRepository.findById(event.getRefundId()).ifPresent(refund -> {
      refund.setRefundStatus("COMPLETED");
      refund.setProcessedAt(Instant.now());
      refundRepository.save(refund);
      log.info("Refund {} status updated to COMPLETED", refund.getRefundId());
    });
  }

  @Transactional
  public void handleRefundFailed(fruitshop.order_service.event.RefundFailedEvent event) {
    refundRepository.findById(event.getRefundId()).ifPresent(refund -> {
      refund.setRefundStatus("FAILED");
      refund.setProcessedAt(Instant.now());
      refundRepository.save(refund);
      log.info("Refund {} status updated to FAILED", refund.getRefundId());
    });
  }
}
