package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Refund;
import fruitshop.order_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/refunds")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    @GetMapping
    public ResponseEntity<List<Refund>> getByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(refundService.findByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<Refund> create(
            @PathVariable String orderId,
            @RequestParam(required = false) String orderItemId,
            @RequestBody Refund refund
    ) {
        return ResponseEntity.ok(refundService.create(orderId, orderItemId, refund));
    }
}
