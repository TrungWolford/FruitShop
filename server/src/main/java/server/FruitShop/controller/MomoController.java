package server.FruitShop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.response.Momo.CreateMomoResponse;
import server.FruitShop.entity.Order;
import server.FruitShop.entity.Payment;
import server.FruitShop.repository.OrderRepository;
import server.FruitShop.repository.PaymentRepository;
import server.FruitShop.service.MomoService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for MoMo payment integration
 */
@RestController
@RequestMapping("/api/momo")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MomoController {

    private final MomoService momoService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Create MoMo QR payment for an order
     * POST /api/momo/create-payment
     * 
     * Request body: { "orderId": "xxx" }
     * 
     * @param request Map containing orderId
     * @return CreateMomoResponse with QR code and payment URL
     */
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");

            if (orderId == null || orderId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required field: orderId"
                ));
            }

            log.info("📱 Creating MoMo payment for orderId: {}", orderId);

            // Find order
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Order not found: " + orderId
                ));
            }

            Order order = orderOpt.get();

            // Check if order is paid
            if (order.getPayment() != null && order.getPayment().getPaymentStatus() == 1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Order already paid"
                ));
            }

            // Check if order is cancelled
            if (order.getStatus() == 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cannot create payment for cancelled order"
                ));
            }

            long amount = order.getTotalAmount();
            String orderInfo = "Thanh toán đơn hàng #" + orderId;

            log.info("💰 Order amount: {} VND", amount);

            // Create MoMo QR payment
            CreateMomoResponse response = momoService.createQR(orderId, amount, orderInfo);

            if (response.isSuccess()) {
                // Update payment with transaction ID
                if (order.getPayment() != null) {
                    Payment payment = order.getPayment();
                    payment.setTransactionId(response.getRequestId());
                    payment.setPaymentStatus(0); // Pending
                    paymentRepository.save(payment);
                    log.info("✅ Updated payment transaction ID: {}", response.getRequestId());
                }

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment created successfully",
                    "data", response
                ));
            } else {
                log.error("❌ MoMo payment creation failed: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage(),
                    "resultCode", response.getResultCode()
                ));
            }

        } catch (Exception e) {
            log.error("💥 Error creating MoMo payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    /**
     * Handle MoMo IPN (Instant Payment Notification) callback
     * POST /api/momo/ipn-handler
     * 
     * This endpoint is called by MoMo server when payment status changes
     */
    @PostMapping("/ipn-handler")
    public ResponseEntity<?> handleIPN(@RequestBody Map<String, Object> ipnData) {
        try {
            log.info("📨 Received MoMo IPN callback: {}", ipnData);

            String orderId = (String) ipnData.get("orderId");
            Integer resultCode = (Integer) ipnData.get("resultCode");
            String message = (String) ipnData.get("message");
            String transactionId = (String) ipnData.get("transId");
            String signature = (String) ipnData.get("signature");
            Object amountObj = ipnData.get("amount");

            // Build raw signature for verification
            String rawSignature = "accessKey=" + ipnData.get("accessKey") +
                    "&amount=" + amountObj +
                    "&extraData=" + ipnData.get("extraData") +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + ipnData.get("orderInfo") +
                    "&orderType=" + ipnData.get("orderType") +
                    "&partnerCode=" + ipnData.get("partnerCode") +
                    "&payType=" + ipnData.get("payType") +
                    "&requestId=" + ipnData.get("requestId") +
                    "&responseTime=" + ipnData.get("responseTime") +
                    "&resultCode=" + resultCode +
                    "&transId=" + transactionId;

            // Verify signature
            boolean isValidSignature = momoService.verifySignature(rawSignature, signature);
            if (!isValidSignature) {
                log.error("❌ Invalid signature from MoMo IPN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Find order
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                log.error("❌ Order not found: {}", orderId);
                return ResponseEntity.noContent().build();
            }

            Order order = orderOpt.get();

            // Update payment status based on MoMo result
            if (resultCode != null && resultCode == 0) {
                // Payment successful
                log.info("✅ Payment successful for orderId: {}", orderId);

                if (order.getPayment() != null) {
                    Payment payment = order.getPayment();
                    payment.setPaymentStatus(1); // Completed
                    payment.setPaymentDate(new Date());
                    payment.setTransactionId(String.valueOf(transactionId));
                    payment.setPaymentMethod("Thanh toán thành công"); // Set payment method
                    
                    // Convert amount to BigDecimal
                    long amount = 0;
                    if (amountObj instanceof Integer) {
                        amount = ((Integer) amountObj).longValue();
                    } else if (amountObj instanceof Long) {
                        amount = (Long) amountObj;
                    }
                    payment.setAmount(BigDecimal.valueOf(amount));
                    
                    paymentRepository.save(payment);
                    log.info("✅ Payment updated to COMPLETED with method: Thanh toán thành công");
                }

                // Keep order status as is (don't change order flow)
                // Admin will confirm and ship the order separately

            } else {
                // Payment failed
                log.warn("❌ Payment failed for orderId: {}. ResultCode: {}, Message: {}", 
                        orderId, resultCode, message);

                if (order.getPayment() != null) {
                    Payment payment = order.getPayment();
                    payment.setPaymentStatus(2); // Failed
                    paymentRepository.save(payment);
                }

                // Set order status to cancelled (0)
                order.setStatus(0);
                orderRepository.save(order);
                log.info("❌ Order cancelled due to payment failure");
            }

            // Return 204 No Content to acknowledge receipt
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("💥 Error handling MoMo IPN", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle MoMo return URL callback
     * GET /api/momo/return
     * 
     * This endpoint is called when user returns from MoMo payment page
     */
    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(@RequestParam Map<String, String> params) {
        try {
            log.info("🔙 Received MoMo return callback: {}", params);

            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");

            // Build response for frontend
            return ResponseEntity.ok(Map.of(
                "success", "0".equals(resultCode),
                "orderId", orderId != null ? orderId : "",
                "message", message != null ? message : "",
                "resultCode", resultCode != null ? resultCode : ""
            ));

        } catch (Exception e) {
            log.error("💥 Error handling MoMo return", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error processing payment result"
            ));
        }
    }

    /**
     * Check payment status for an order
     * GET /api/momo/check-status/{orderId}
     */
    @GetMapping("/check-status/{orderId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String orderId) {
        try {
            log.info("🔍 Checking payment status for orderId: {}", orderId);

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Order not found"
                ));
            }

            Order order = orderOpt.get();
            Payment payment = order.getPayment();

            String status = "UNKNOWN";
            if (payment != null) {
                switch (payment.getPaymentStatus()) {
                    case 0: status = "PENDING"; break;
                    case 1: status = "COMPLETED"; break;
                    case 2: status = "FAILED"; break;
                    case 3: status = "REFUNDED"; break;
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "paymentStatus", status,
                "orderStatus", order.getStatus(),
                "totalAmount", order.getTotalAmount()
            ));

        } catch (Exception e) {
            log.error("💥 Error checking payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error checking payment status"
            ));
        }
    }
}
