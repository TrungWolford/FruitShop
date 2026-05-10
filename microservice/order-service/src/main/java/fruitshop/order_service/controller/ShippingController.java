package fruitshop.order_service.controller;

import fruitshop.order_service.dto.request.ShippingRequest;
import fruitshop.order_service.dto.response.ShippingResponse;
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
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShippingResponse> getShipping(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.findByOrderId(orderId));
    }

    @PutMapping("/order/{orderId}")
    public ResponseEntity<ShippingResponse> upsert(
            @PathVariable String orderId,
            @RequestBody ShippingRequest request
    ) {
        return ResponseEntity.ok(shippingService.upsert(orderId, request));
    }

    @GetMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> getShippingById(@PathVariable String shippingId) {
        return ResponseEntity.ok(shippingService.getShippingById(shippingId));
    }

    @PostMapping
    public ResponseEntity<ShippingResponse> createShipping(@RequestBody ShippingRequest request) {
        // Create without orderId initially if needed, or implement appropriate logic
        return ResponseEntity.ok(shippingService.createShipping(request));
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ShippingResponse> createShippingWithOrder(
            @PathVariable String orderId,
            @RequestBody ShippingRequest request
    ) {
        return ResponseEntity.ok(shippingService.createShipping(orderId, request));
    }

    @PutMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> updateShipping(
            @PathVariable String shippingId,
            @RequestBody ShippingRequest request
    ) {
        return ResponseEntity.ok(shippingService.updateShipping(shippingId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ShippingResponse>> getAllShippings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "shippingId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(shippingService.getAllShippingsPaginated(pageable));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ShippingResponse>> getShippingsByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(shippingService.getShippingsByAccountId(accountId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ShippingResponse>> getShippingsByStatus(
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

    @GetMapping("/search")
    public ResponseEntity<Page<ShippingResponse>> searchAndFilterShippings(
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

    @PutMapping("/{shippingId}/status")
    public ResponseEntity<ShippingResponse> updateShippingStatus(
            @PathVariable String shippingId,
            @RequestParam int status
    ) {
        return ResponseEntity.ok(shippingService.updateShippingStatus(shippingId, status));
    }

    @DeleteMapping("/{shippingId}")
    public ResponseEntity<Void> deleteShipping(@PathVariable String shippingId) {
        shippingService.deleteShipping(shippingId);
        return ResponseEntity.noContent().build();
    }
}
