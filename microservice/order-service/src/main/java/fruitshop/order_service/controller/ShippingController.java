package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/{orderId}/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @GetMapping
    public ResponseEntity<Shipping> getShipping(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.findByOrderId(orderId));
    }

    @PutMapping
    public ResponseEntity<Shipping> upsert(
            @PathVariable String orderId,
            @RequestBody Shipping shipping
    ) {
        return ResponseEntity.ok(shippingService.upsert(orderId, shipping));
    }
}
