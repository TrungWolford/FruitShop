package server.FruitShop.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Payment.PaymentRequest;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.entity.Payment;
import server.FruitShop.repository.PaymentRepository;
import server.FruitShop.service.PaymentService;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;

    /**
     * Lấy tất cả payment với phân trang
     */
    @Override
    public Page<PaymentResponse> getAllPayment(Pageable pageable) {
        log.info("Getting all payments with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Payment> payments = paymentRepository.findAll(pageable);
        
        return payments.map(PaymentResponse::fromEntity);
    }

    /**
     * Lấy payment theo ID
     */
    @Override
    public PaymentResponse getByPaymentId(String paymentId) {
        log.info("Getting payment by ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new RuntimeException("Payment not found with ID: " + paymentId);
                });
        
        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Tạo payment mới
     */
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating new payment with method: {}, amount: {}", 
                request.getPaymentMethod(), request.getAmount());
        
        // Validate request
        validatePaymentRequest(request);
        
        // Create new payment entity
        Payment payment = new Payment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(request.getPaymentStatus());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : new Date());
        payment.setAmount(request.getAmount());
        payment.setTransactionId(request.getTransactionId());
        
        // Save to database
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment created successfully with ID: {}", savedPayment.getPaymentId());
        
        return PaymentResponse.fromEntity(savedPayment);
    }

    /**
     * Cập nhật payment (có thể mở rộng thêm các tham số cần update)
     * Hiện tại chưa có logic cụ thể vì interface chưa định nghĩa parameters
     */
    @Override
    @Transactional
    public PaymentResponse updatePayment() {
        log.warn("updatePayment() called but not implemented yet");
        throw new UnsupportedOperationException("Update payment method needs to be implemented with specific parameters");
    }

    /**
     * Cập nhật payment theo ID
     */
    @Override
    @Transactional
    public PaymentResponse updatePayment(String paymentId, PaymentRequest request) {
        log.info("Updating payment with ID: {}", paymentId);
        
        // Find existing payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new RuntimeException("Payment not found with ID: " + paymentId);
                });
        
        // Validate request
        validatePaymentRequest(request);
        
        // Update fields
        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        payment.setPaymentStatus(request.getPaymentStatus());
        
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
        }
        
        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
        }
        
        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }
        
        // Save to database
        Payment updatedPayment = paymentRepository.save(payment);
        
        log.info("Payment updated successfully with ID: {}", updatedPayment.getPaymentId());
        
        return PaymentResponse.fromEntity(updatedPayment);
    }

    /**
     * Lấy payment theo status
     */
    @Override
    public Page<PaymentResponse> getPaymentsByStatus(int status, Pageable pageable) {
        log.info("Getting payments by status: {}", status);
        
        // Validate status
        if (status < 0 || status > 3) {
            log.error("Invalid payment status: {}", status);
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }
        
        Page<Payment> payments = paymentRepository.findByPaymentStatus(status, pageable);
        
        log.info("Found {} payments with status {}", payments.getTotalElements(), status);
        
        return payments.map(PaymentResponse::fromEntity);
    }

    /**
     * Cập nhật trạng thái payment
     */
    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, int status) {
        log.info("Updating payment status for ID: {} to status: {}", paymentId, status);
        
        // Validate status
        if (status < 0 || status > 3) {
            log.error("Invalid payment status: {}", status);
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }
        
        // Find existing payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found with ID: {}", paymentId);
                    return new RuntimeException("Payment not found with ID: " + paymentId);
                });
        
        // Update status
        payment.setPaymentStatus(status);
        
        // Save to database
        Payment updatedPayment = paymentRepository.save(payment);
        
        log.info("Payment status updated successfully for ID: {}", updatedPayment.getPaymentId());
        
        return PaymentResponse.fromEntity(updatedPayment);
    }

    /**
     * Lấy payment theo transaction ID
     */
    @Override
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        log.info("Getting payment by transaction ID: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    log.error("Payment not found with transaction ID: {}", transactionId);
                    return new RuntimeException("Payment not found with transaction ID: " + transactionId);
                });
        
        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Validate payment request
     */
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            log.error("Payment method is required");
            throw new IllegalArgumentException("Payment method is required");
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.error("Payment amount must be greater than 0");
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        
        // Validate payment status (0-3)
        if (request.getPaymentStatus() < 0 || request.getPaymentStatus() > 3) {
            log.error("Invalid payment status: {}", request.getPaymentStatus());
            throw new IllegalArgumentException("Payment status must be between 0 and 3");
        }
    }
}
