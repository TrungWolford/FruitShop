package fruitshop.payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fruitshop.payment_service.dto.request.Payment.PaymentRequest;
import fruitshop.payment_service.dto.response.Payment.PaymentResponse;
import fruitshop.payment_service.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentResponse> payments = paymentService.getAllPayment(pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String paymentId) {
        PaymentResponse payment = paymentService.getByPaymentId(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable String paymentId,
            @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.updatePayment(paymentId, request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(
            @PathVariable int status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        validatePaymentStatus(status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        Page<PaymentResponse> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam int status) {
        validatePaymentStatus(status);

        PaymentResponse payment = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(payment);
    }

    private void validatePaymentStatus(int status) {
        if (status < 0 || status > 3) {
            throw new IllegalArgumentException("Invalid payment status. Must be between 0 and 3");
        }
    }
}