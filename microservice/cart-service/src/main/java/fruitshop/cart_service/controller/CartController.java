package fruitshop.cart_service.controller;

import fruitshop.cart_service.entity.Cart;
import fruitshop.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{accountId}")
    public ResponseEntity<Cart> getCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(accountId));
    }

    @PostMapping("/{accountId}/items")
    public ResponseEntity<Cart> addItem(
            @PathVariable String accountId,
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return ResponseEntity.ok(cartService.addItem(accountId, productId, quantity));
    }

    @PutMapping("/{accountId}/items/{cartItemId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable String accountId,
            @PathVariable String cartItemId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(accountId, cartItemId, quantity));
    }

    @DeleteMapping("/{accountId}/items/{cartItemId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable String accountId,
            @PathVariable String cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItem(accountId, cartItemId));
    }

    @DeleteMapping("/{accountId}/items")
    public ResponseEntity<Cart> clearCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.clearCart(accountId));
    }

    @PutMapping("/{cartId}/status")
    public ResponseEntity<Cart> updateCartStatus(
            @PathVariable String cartId,
            @RequestParam int status
    ) {
        return ResponseEntity.ok(cartService.updateCartStatus(cartId, status));
    }

    @PutMapping("/{cartId}/enable")
    public ResponseEntity<Cart> enableCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.enableCart(cartId));
    }

    @PutMapping("/{cartId}/disable")
    public ResponseEntity<Cart> disableCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.disableCart(cartId));
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<Cart>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return ResponseEntity.ok(cartService.getAllCart(pageable));
    }

    @GetMapping("/id/{cartId}")
    public ResponseEntity<Cart> getCartById(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
