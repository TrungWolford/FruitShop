package fruitshop.payment_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.dto.response.Payment.PaymentResponse;

public interface PaymentService {
    Page<PaymentResponse> getAllPayment(Pageable pageable);
    PaymentResponse getByPaymentId(String paymentId);
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse updatePayment();
    PaymentResponse updatePayment(String paymentId, PaymentRequest request);
    Page<PaymentResponse> getPaymentsByStatus(int status, Pageable pageable);
    PaymentResponse updatePaymentStatus(String paymentId, int status);
    PaymentResponse getPaymentByTransactionId(String transactionId);
}