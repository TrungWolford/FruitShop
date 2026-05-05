package server.FruitShop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Payment.PaymentRequest;
import server.FruitShop.dto.response.Payment.PaymentResponse;
import server.FruitShop.service.PaymentService;

/**
 * Controller để quản lý Payment (Thanh toán)
 * 
 * Endpoints:
 * - GET    /api/payment                  - Lấy tất cả payment với phân trang
 * - GET    /api/payment/{paymentId}      - Lấy payment theo ID
 * - POST   /api/payment                  - Tạo payment mới
 * - PUT    /api/payment/{paymentId}      - Cập nhật payment
 * - GET    /api/payment/status/{status}  - Lấy payment theo status
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Lấy tất cả payment với phân trang và sắp xếp
     * 
     * @param page Số trang (mặc định: 0)
     * @param size Kích thước trang (mặc định: 10)
     * @param sortBy Trường để sắp xếp (mặc định: paymentDate)
     * @param sortDir Hướng sắp xếp: asc/desc (mặc định: desc)
     * @return Page<PaymentResponse>
     * 
     * Example: GET /api/payment?page=0&size=10&sortBy=paymentDate&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("GET /api/payment - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<PaymentResponse> payments = paymentService.getAllPayment(pageable);
            
            log.info("Successfully retrieved {} payments", payments.getTotalElements());
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error getting all payments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy payment theo ID
     * 
     * @param paymentId ID của payment
     * @return PaymentResponse
     * 
     * Example: GET /api/payment/123e4567-e89b-12d3-a456-426614174000
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable String paymentId) {
        log.info("GET /api/payment/{} - Getting payment by ID", paymentId);
        
        try {
            PaymentResponse payment = paymentService.getByPaymentId(paymentId);
            log.info("Successfully retrieved payment: {}", paymentId);
            return ResponseEntity.ok(payment);
            
        } catch (RuntimeException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found with ID: " + paymentId);
                    
        } catch (Exception e) {
            log.error("Error getting payment by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving payment: " + e.getMessage());
        }
    }

    /**
     * Tạo payment mới
     * 
     * @param request PaymentRequest
     * @return PaymentResponse
     * 
     * Example: POST /api/payment
     * Body:
     * {
     *   "paymentMethod": "COD",
     *   "paymentStatus": 0,
     *   "amount": 100000,
     *   "transactionId": "TXN123456"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        log.info("POST /api/payment - Creating new payment with method: {}, amount: {}", 
                request.getPaymentMethod(), request.getAmount());
        
        try {
            PaymentResponse payment = paymentService.createPayment(request);
            log.info("Payment created successfully with ID: {}", payment.getPaymentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
                    
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating payment: " + e.getMessage());
        }
    }

    /**
     * Cập nhật payment theo ID
     * 
     * @param paymentId ID của payment cần cập nhật
     * @param request PaymentRequest với thông tin mới
     * @return PaymentResponse
     * 
     * Example: PUT /api/payment/123e4567-e89b-12d3-a456-426614174000
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updatePayment(
            @PathVariable String paymentId,
            @RequestBody PaymentRequest request) {
        
        log.info("PUT /api/payment/{} - Updating payment", paymentId);
        
        try {
            PaymentResponse payment = paymentService.updatePayment(paymentId, request);
            log.info("Payment updated successfully: {}", paymentId);
            return ResponseEntity.ok(payment);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
                    
        } catch (RuntimeException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found with ID: " + paymentId);
                    
        } catch (Exception e) {
            log.error("Error updating payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating payment: " + e.getMessage());
        }
    }

    /**
     * Lấy payment theo status
     * 
     * @param status Status của payment (0: Pending, 1: Completed, 2: Failed, 3: Refunded)
     * @param page Số trang (mặc định: 0)
     * @param size Kích thước trang (mặc định: 10)
     * @return Page<PaymentResponse>
     * 
     * Example: GET /api/payment/status/1?page=0&size=10
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(
            @PathVariable int status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/payment/status/{} - Getting payments by status", status);
        
        try {
            // Validate status
            if (status < 0 || status > 3) {
                log.error("Invalid payment status: {}", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid payment status. Must be between 0 and 3");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
            Page<PaymentResponse> payments = paymentService.getPaymentsByStatus(status, pageable);
            
            log.info("Successfully retrieved {} payments with status {}", payments.getTotalElements(), status);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error getting payments by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving payments: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái payment
     * 
     * @param paymentId ID của payment
     * @param status Status mới (0: Pending, 1: Completed, 2: Failed, 3: Refunded)
     * @return PaymentResponse
     * 
     * Example: PUT /api/payment/123e4567-e89b-12d3-a456-426614174000/status?status=1
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam int status) {
        
        log.info("PUT /api/payment/{}/status - Updating payment status to {}", paymentId, status);
        
        try {
            // Validate status
            if (status < 0 || status > 3) {
                log.error("Invalid payment status: {}", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid payment status. Must be between 0 and 3");
            }
            
            PaymentResponse payment = paymentService.updatePaymentStatus(paymentId, status);
            log.info("Payment status updated successfully for ID: {}", paymentId);
            return ResponseEntity.ok(payment);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
                    
        } catch (RuntimeException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found with ID: " + paymentId);
                    
        } catch (Exception e) {
            log.error("Error updating payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating payment status: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin payment theo transaction ID
     * 
     * @param transactionId Transaction ID từ cổng thanh toán
     * @return PaymentResponse
     * 
     * Example: GET /api/payment/transaction/TXN123456
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("GET /api/payment/transaction/{} - Getting payment by transaction ID", transactionId);
        
        try {
            PaymentResponse payment = paymentService.getPaymentByTransactionId(transactionId);
            log.info("Successfully retrieved payment with transaction ID: {}", transactionId);
            return ResponseEntity.ok(payment);
            
        } catch (RuntimeException e) {
            log.error("Payment not found with transaction ID: {}", transactionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found with transaction ID: " + transactionId);
                    
        } catch (Exception e) {
            log.error("Error getting payment by transaction ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving payment: " + e.getMessage());
        }
    }
}
