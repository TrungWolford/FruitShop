package fruitshop.cart_service.controller;

import fruitshop.cart_service.dto.request.CreateCartItemRequest;
import fruitshop.cart_service.dto.request.UpdateCartItemRequest;
import fruitshop.cart_service.dto.response.CartItemResponse;
import fruitshop.cart_service.dto.response.CartResponse;
import fruitshop.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(accountId));
    }

    @GetMapping("/account/{accountId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.getCartItemsByAccountId(accountId));
    }

    @PostMapping("/account/{accountId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable String accountId,
            @RequestBody CreateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(accountId, request));
    }

    @PutMapping("/account/{accountId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable String accountId,
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(accountId, cartItemId, request));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantityOnly(
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantityByItemId(cartItemId, request));
    }

    @DeleteMapping("/account/{accountId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String accountId,
            @PathVariable String cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItem(accountId, cartItemId));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemOnly(
            @PathVariable String cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItemByItemId(cartItemId));
    }

    @DeleteMapping("/account/{accountId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.clearCart(accountId));
    }

    @DeleteMapping("/account/{accountId}/clear")
    public ResponseEntity<CartResponse> clearCartAlternative(@PathVariable String accountId) {
        return ResponseEntity.ok(cartService.clearCart(accountId));
    }

    @PutMapping("/{cartId}/status")
    public ResponseEntity<CartResponse> updateCartStatus(
            @PathVariable String cartId,
            @RequestParam int status
    ) {
        return ResponseEntity.ok(cartService.updateCartStatus(cartId, status));
    }

    @PutMapping("/{cartId}/status/{status}")
    public ResponseEntity<CartResponse> updateCartStatusAlternative(
            @PathVariable String cartId,
            @PathVariable int status
    ) {
        return ResponseEntity.ok(cartService.updateCartStatus(cartId, status));
    }

    @PutMapping("/{cartId}/enable")
    public ResponseEntity<CartResponse> enableCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.enableCart(cartId));
    }

    @PutMapping("/{cartId}/disable")
    public ResponseEntity<CartResponse> disableCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.disableCart(cartId));
    }

    @GetMapping
    public ResponseEntity<Page<CartResponse>> getAllCarts(
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
    public ResponseEntity<CartResponse> getCartById(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
