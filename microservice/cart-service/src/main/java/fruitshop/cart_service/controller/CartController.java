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
}
