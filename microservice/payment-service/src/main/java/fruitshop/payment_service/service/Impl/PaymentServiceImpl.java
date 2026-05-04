package fruitshop.payment_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.dto.response.Payment.PaymentResponse;
import fruitshop.payment_service.entity.Payment;
import fruitshop.payment_service.exception.DownstreamServiceException;
import fruitshop.payment_service.feign.OrderClient;
import fruitshop.payment_service.feign.dto.OrderSummaryDto;
import fruitshop.payment_service.exception.ResourceNotFoundException;
import fruitshop.payment_service.repository.PaymentRepository;
import fruitshop.payment_service.service.PaymentService;
import org.springframework.cloud.stream.function.StreamBridge;
import fruitshop.payment_service.event.PaymentCompletedEvent;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final StreamBridge streamBridge;

    @Override
    public Page<PaymentResponse> getAllPayment(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentResponse::fromEntity);
    }

    @Override
    public PaymentResponse getByPaymentId(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        validatePaymentRequest(request);
        validateOrderIfPresent(request.getOrderId());

        Payment payment = new Payment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(request.getPaymentStatus());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : new Date());
        payment.setAmount(request.getAmount());
        payment.setTransactionId(request.getTransactionId());
        payment.setOrderId(request.getOrderId());

        return PaymentResponse.fromEntity(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment() {
        throw new UnsupportedOperationException("Update payment method needs to be implemented with specific parameters");
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(String paymentId, PaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        validatePaymentRequest(request);
        validateOrderIfPresent(request.getOrderId());

        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        payment.setPaymentStatus(request.getPaymentStatus());
        if (request.getPaymentDate() != null) payment.setPaymentDate(request.getPaymentDate());
        if (request.getAmount() != null) payment.setAmount(request.getAmount());
        if (request.getTransactionId() != null) payment.setTransactionId(request.getTransactionId());
        payment.setOrderId(request.getOrderId());

        return PaymentResponse.fromEntity(paymentRepository.save(payment));
    }

    @Override
    public Page<PaymentResponse> getPaymentsByStatus(int status, Pageable pageable) {
        if (status < 0 || status > 3) {
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }
        return paymentRepository.findByPaymentStatus(status, pageable).map(PaymentResponse::fromEntity);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, int status) {
        if (status < 0 || status > 3) {
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        payment.setPaymentStatus(status);
        Payment savedPayment = paymentRepository.save(payment);

        if (status == 1) { // Assuming 1 = COMPLETED
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    savedPayment.getPaymentId(),
                    savedPayment.getOrderId(),
                    savedPayment.getAmount(),
                    savedPayment.getTransactionId(),
                    new Date()
            );
            streamBridge.send("paymentCompletedSupplier-out-0", event);
            log.info("Published PaymentCompletedEvent for paymentId: {}", savedPayment.getPaymentId());
        }

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));
        return PaymentResponse.fromEntity(payment);
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (request.getPaymentStatus() < 0 || request.getPaymentStatus() > 3) {
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }
    }

    private void validateOrderIfPresent(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        try {
            OrderSummaryDto order = orderClient.getById(orderId);
            if (order == null || order.getOrderId() == null) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Order service unavailable while validating orderId: " + orderId);
        }
    }
}