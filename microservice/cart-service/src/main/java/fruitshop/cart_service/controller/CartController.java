package fruitshop.cart_service.controller;

import fruitshop.cart_service.dto.CartItemRequest;
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

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Cart> getCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(accountId));
    }

    @PostMapping("/account/{accountId}/items")
    public ResponseEntity<Cart> addItem(
            @PathVariable String accountId,
            @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(accountId, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/account/{accountId}/items/{cartItemId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable String accountId,
            @PathVariable String cartItemId,
            @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(accountId, cartItemId, request.getQuantity()));
    }

    @DeleteMapping("/account/{accountId}/items/{cartItemId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable String accountId,
            @PathVariable String cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItem(accountId, cartItemId));
    }

    @DeleteMapping("/account/{accountId}/items")
    public ResponseEntity<Cart> clearCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.clearCart(accountId));
    }
}
