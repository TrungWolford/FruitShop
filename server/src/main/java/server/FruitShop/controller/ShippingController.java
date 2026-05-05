package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.dto.response.Shipping.ShippingResponse;
import server.FruitShop.service.ShippingService;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "*")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    // Get all shippings (with optional pagination)
    @GetMapping("")
    public ResponseEntity<List<ShippingResponse>> getAllShippings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<ShippingResponse> shippingsPage = shippingService.getAllShippingsPaginated(pageable);
            return ResponseEntity.ok(shippingsPage.getContent());
        }
        List<ShippingResponse> shippings = shippingService.getAllShippings();
        return ResponseEntity.ok(shippings);
    }

    // Get shippings by account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ShippingResponse>> getShippingsByAccount(@PathVariable String accountId) {
        try {
            List<ShippingResponse> shippings = shippingService.getShippingsByAccountId(accountId);
            return ResponseEntity.ok(shippings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get shipping by ID
    @GetMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> getShippingById(@PathVariable String shippingId) {
        try {
            ShippingResponse shipping = shippingService.getShippingById(shippingId);
            return ResponseEntity.ok(shipping);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Create new shipping
    @PostMapping("")
    public ResponseEntity<ShippingResponse> createShipping(@RequestBody ShippingRequest request) {
        try {
            ShippingResponse shipping = shippingService.createShipping(request);
            return ResponseEntity.ok(shipping);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Update shipping
    @PutMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> updateShipping(
            @PathVariable String shippingId,
            @RequestBody ShippingRequest request) {
        try {
            ShippingResponse shipping = shippingService.updateShipping(shippingId, request);
            return ResponseEntity.ok(shipping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete shipping
    @DeleteMapping("/{shippingId}")
    public ResponseEntity<Void> deleteShipping(@PathVariable String shippingId) {
        try {
            shippingService.deleteShipping(shippingId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update shipping status
    @PutMapping("/{shippingId}/status")
    public ResponseEntity<ShippingResponse> updateShippingStatus(
            @PathVariable String shippingId,
            @RequestParam int status) {
        try {
            ShippingResponse shipping = shippingService.updateShippingStatus(shippingId, status);
            return ResponseEntity.ok(shipping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Filter shippings by status
    @GetMapping("/filter")
    public ResponseEntity<Page<ShippingResponse>> filterShippingsByStatus(
            @RequestParam int status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ShippingResponse> shippings = shippingService.getShippingsByStatus(status, pageable);
            return ResponseEntity.ok(shippings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Search shippings
    @GetMapping("/search")
    public ResponseEntity<Page<ShippingResponse>> searchShippings(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ShippingResponse> shippings = shippingService.searchShippings(keyword, pageable);
            return ResponseEntity.ok(shippings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Search and filter shippings
    @GetMapping("/search-filter")
    public ResponseEntity<Page<ShippingResponse>> searchAndFilterShippings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ShippingResponse> shippings = shippingService.searchAndFilterShippings(keyword, status, pageable);
            return ResponseEntity.ok(shippings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
