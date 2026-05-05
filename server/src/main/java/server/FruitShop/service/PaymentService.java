package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Payment.PaymentRequest;
import server.FruitShop.dto.response.Payment.PaymentResponse;

public interface PaymentService {
    /**
     * Lấy tất cả payment với phân trang
     */
    Page<PaymentResponse> getAllPayment(Pageable pageable);

    /**
     * Lấy payment theo ID
     */
    PaymentResponse getByPaymentId(String paymentId);

    /**
     * Tạo payment mới
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Cập nhật payment
     * TODO: Cần thêm parameters cụ thể
     */
    PaymentResponse updatePayment();

    /**
     * Cập nhật payment theo ID
     */
    PaymentResponse updatePayment(String paymentId, PaymentRequest request);

    /**
     * Lấy payment theo status
     */
    Page<PaymentResponse> getPaymentsByStatus(int status, Pageable pageable);

    /**
     * Cập nhật trạng thái payment
     */
    PaymentResponse updatePaymentStatus(String paymentId, int status);

    /**
     * Lấy payment theo transaction ID
     */
    PaymentResponse getPaymentByTransactionId(String transactionId);
}

