package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Refund.CreateRefundRequest;
import server.FruitShop.dto.request.Refund.UpdateRefundStatusRequest;
import server.FruitShop.dto.response.Refund.RefundResponse;
import server.FruitShop.service.RefundService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/refund")
@CrossOrigin(origins = "*")
public class RefundController {

    @Autowired
    private RefundService refundService;

    // Customer endpoints - Create refund request
    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@RequestBody CreateRefundRequest request) {
        try {
            System.out.println("🔔 Incoming CreateRefundRequest: " + request);
            RefundResponse refund = refundService.createRefund(request);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin endpoints - Get all refunds with pagination
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RefundResponse> refundPage = refundService.getAllRefunds(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", refundPage.getContent());
            response.put("totalPages", refundPage.getTotalPages());
            response.put("totalElements", refundPage.getTotalElements());
            response.put("currentPage", refundPage.getNumber());
            response.put("size", refundPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Get refund by ID
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable String refundId) {
        try {
            RefundResponse refund = refundService.getRefundById(refundId);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get refunds by status
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getRefundsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RefundResponse> refundPage = refundService.getRefundsByStatus(status, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", refundPage.getContent());
            response.put("totalPages", refundPage.getTotalPages());
            response.put("totalElements", refundPage.getTotalElements());
            response.put("currentPage", refundPage.getNumber());
            response.put("size", refundPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Get refunds by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByOrderId(@PathVariable String orderId) {
        try {
            List<RefundResponse> refunds = refundService.getRefundsByOrderId(orderId);
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get refunds by order item ID (per-item refund)
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByOrderItemId(@PathVariable String orderItemId) {
        try {
            List<RefundResponse> refunds = refundService.getRefundsByOrderItemId(orderItemId);
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Search refunds
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRefunds(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RefundResponse> refundPage = refundService.searchRefunds(keyword, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", refundPage.getContent());
            response.put("totalPages", refundPage.getTotalPages());
            response.put("totalElements", refundPage.getTotalElements());
            response.put("currentPage", refundPage.getNumber());
            response.put("size", refundPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Get refunds by date range
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getRefundsByDateRange(
            @RequestParam Long startDate,
            @RequestParam Long endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Date start = new Date(startDate);
            Date end = new Date(endDate);
            Pageable pageable = PageRequest.of(page, size);
            Page<RefundResponse> refundPage = refundService.getRefundsByDateRange(start, end, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", refundPage.getContent());
            response.put("totalPages", refundPage.getTotalPages());
            response.put("totalElements", refundPage.getTotalElements());
            response.put("currentPage", refundPage.getNumber());
            response.put("size", refundPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin - Update refund status
    @PutMapping("/{refundId}/status")
    public ResponseEntity<RefundResponse> updateRefundStatus(
            @PathVariable String refundId,
            @RequestBody UpdateRefundStatusRequest request) {
        try {
            RefundResponse refund = refundService.updateRefundStatus(request, refundId);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin - Approve refund
    @PutMapping("/{refundId}/approve")
    public ResponseEntity<RefundResponse> approveRefund(@PathVariable String refundId) {
        try {
            RefundResponse refund = refundService.approveRefund(refundId);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin - Reject refund
    @PutMapping("/{refundId}/reject")
    public ResponseEntity<RefundResponse> rejectRefund(@PathVariable String refundId) {
        try {
            RefundResponse refund = refundService.rejectRefund(refundId);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin - Complete refund
    @PutMapping("/{refundId}/complete")
    public ResponseEntity<RefundResponse> completeRefund(@PathVariable String refundId) {
        try {
            RefundResponse refund = refundService.completeRefund(refundId);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin - Cancel refund
    @DeleteMapping("/{refundId}")
    public ResponseEntity<Void> cancelRefund(@PathVariable String refundId) {
        try {
            refundService.cancelRefund(refundId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Get pending refunds count
    @GetMapping("/stats/pending-count")
    public ResponseEntity<Map<String, Long>> getPendingRefundsCount() {
        try {
            long count = refundService.countPendingRefunds();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
