    package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Refund;
import fruitshop.order_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    // --- Order-scoped endpoints (Backward Compatibility) ---
    
    @GetMapping(value = {"/orders/{orderId}/refunds", "/refunds/order/{orderId}"})
    public ResponseEntity<List<Refund>> getByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(refundService.findByOrderId(orderId));
    }

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<Refund> create(
            @PathVariable String orderId,
            @RequestParam(required = false) String orderItemId,
            @RequestBody Refund refund
    ) {
        return ResponseEntity.ok(refundService.create(orderId, orderItemId, refund));
    }

    @PutMapping("/orders/{orderId}/refunds/{refundId}/approve")
    public ResponseEntity<Refund> approveRefund(
            @PathVariable String orderId,
            @PathVariable String refundId,
            @RequestParam String approverName) {
        return ResponseEntity.ok(refundService.approveRefund(orderId, refundId, approverName));
    }

    // --- Admin endpoints ---

    @PostMapping("/refunds")
    public ResponseEntity<Refund> createRefundDirect(@RequestBody java.util.Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        String orderItemId = (String) body.get("orderItemId");
        
        Refund refund = new Refund();
        refund.setReason((String) body.get("reason"));
        if (body.get("refundAmount") != null) {
            refund.setRefundAmount(((Number) body.get("refundAmount")).longValue());
        }
        if (body.get("imageUrls") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<String> imageUrls = (java.util.List<String>) body.get("imageUrls");
            refund.setImageUrls(String.join(",", imageUrls));
        }
        refund.setOriginalPaymentId((String) body.get("originalPaymentId"));
        
        return ResponseEntity.ok(refundService.create(orderId, orderItemId, refund));
    }

    @GetMapping("/refunds")
    public ResponseEntity<Page<Refund>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(refundService.getAllRefunds(pageable));
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<Refund> getRefundById(@PathVariable String refundId) {
        return ResponseEntity.ok(refundService.getRefundById(refundId));
    }

    @GetMapping("/refunds/status/{status}")
    public ResponseEntity<Page<Refund>> getRefundsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(refundService.getRefundsByStatus(status, pageable));
    }

    @GetMapping("/refunds/search")
    public ResponseEntity<Page<Refund>> searchRefunds(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(refundService.searchRefunds(keyword, pageable));
    }

    @GetMapping("/refunds/daterange")
    public ResponseEntity<Page<Refund>> getRefundsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Instant start = Instant.parse(startDate);
        Instant end = Instant.parse(endDate);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(refundService.getRefundsByDateRange(start, end, pageable));
    }

    @GetMapping("/refunds/pending/count")
    public ResponseEntity<Long> countPendingRefunds() {
        return ResponseEntity.ok(refundService.countPendingRefunds());
    }

    @PutMapping("/refunds/{refundId}/reject")
    public ResponseEntity<Refund> rejectRefund(@PathVariable String refundId) {
        return ResponseEntity.ok(refundService.rejectRefund(refundId));
    }

    @PutMapping("/refunds/{refundId}/complete")
    public ResponseEntity<Refund> completeRefund(@PathVariable String refundId) {
        return ResponseEntity.ok(refundService.completeRefund(refundId));
    }

    @PutMapping("/refunds/{refundId}/cancel")
    public ResponseEntity<Void> cancelRefund(@PathVariable String refundId) {
        refundService.cancelRefund(refundId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/refunds/{refundId}")
    public ResponseEntity<Void> deleteRefund(@PathVariable String refundId) {
        refundService.cancelRefund(refundId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = {"/orders/items/{orderItemId}/refunds", "/refunds/order-item/{orderItemId}"})
    public ResponseEntity<List<Refund>> getByOrderItem(@PathVariable String orderItemId) {
        return ResponseEntity.ok(refundService.getRefundsByOrderItemId(orderItemId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
