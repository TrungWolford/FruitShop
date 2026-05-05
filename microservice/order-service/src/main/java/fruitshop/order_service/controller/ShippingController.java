package fruitshop.order_service.controller;

import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    // --- Original methods mapped to the old paths ---

    @GetMapping("/orders/{orderId}/shipping")
    public ResponseEntity<Shipping> getShipping(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.findByOrderId(orderId));
    }

    @PutMapping("/orders/{orderId}/shipping")
    public ResponseEntity<Shipping> upsert(
            @PathVariable String orderId,
            @RequestBody Shipping shipping
    ) {
        return ResponseEntity.ok(shippingService.upsert(orderId, shipping));
    }

    // --- New features under /shippings ---

    @GetMapping("/shippings/{shippingId}")
    public ResponseEntity<Shipping> getShippingById(@PathVariable String shippingId) {
        return ResponseEntity.ok(shippingService.getShippingById(shippingId));
    }

    @PostMapping("/orders/{orderId}/shipping")
    public ResponseEntity<Shipping> createShipping(
            @PathVariable String orderId,
            @RequestBody Shipping shipping
    ) {
        return ResponseEntity.ok(shippingService.createShipping(orderId, shipping));
    }

    @PutMapping("/shippings/{shippingId}")
    public ResponseEntity<Shipping> updateShipping(
            @PathVariable String shippingId,
            @RequestBody Shipping shipping
    ) {
        return ResponseEntity.ok(shippingService.updateShipping(shippingId, shipping));
    }

    @GetMapping("/shippings")
    public ResponseEntity<Page<Shipping>> getAllShippings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "shippingId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(shippingService.getAllShippingsPaginated(pageable));
    }

    @GetMapping("/shippings/account/{accountId}")
    public ResponseEntity<List<Shipping>> getShippingsByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(shippingService.getShippingsByAccountId(accountId));
    }

    @GetMapping("/shippings/status/{status}")
    public ResponseEntity<Page<Shipping>> getShippingsByStatus(
            @PathVariable int status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "shippingId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(shippingService.getShippingsByStatus(status, pageable));
    }

    @GetMapping("/shippings/search")
    public ResponseEntity<Page<Shipping>> searchAndFilterShippings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "shippingId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(shippingService.searchAndFilterShippings(keyword, status, pageable));
    }

    @PutMapping("/shippings/{shippingId}/status")
    public ResponseEntity<Shipping> updateShippingStatus(
            @PathVariable String shippingId,
            @RequestParam int status
    ) {
        return ResponseEntity.ok(shippingService.updateShippingStatus(shippingId, status));
    }

    @DeleteMapping("/shippings/{shippingId}")
    public ResponseEntity<Void> deleteShipping(@PathVariable String shippingId) {
        shippingService.deleteShipping(shippingId);
        return ResponseEntity.noContent().build();
    }
}
