package server.FruitShop.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Cart.CartResponse;
import server.FruitShop.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    // Cart endpoints - requires CUSTOMER role
    // @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<CartResponse> getCartByAccountId(@PathVariable String accountId) {
        CartResponse cart = cartService.getCartByAccountId(accountId);
        if (cart != null) {
            return ResponseEntity.ok(cart);
        }
        return ResponseEntity.notFound().build();
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/account/{accountId}")
    public ResponseEntity<CartResponse> createCart(@PathVariable String accountId) {
        try {
            CartResponse cart = cartService.createCart(accountId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.ok().build();
    }

    // CartItem endpoints - requires CUSTOMER role
    // @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/account/{accountId}/items")
    public ResponseEntity<CartItemResponse> addCartItem(
            @PathVariable String accountId,
            @RequestBody CreateCartItemRequest request) {
        try {
            CartItemResponse cartItem = cartService.addCartItem(accountId, request);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartItemResponse cartItem = cartService.updateCartItem(cartItemId, request);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable String cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/account/{accountId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItemsByAccountId(@PathVariable String accountId) {
        List<CartItemResponse> items = cartService.getCartItemsByAccountId(accountId);
        return ResponseEntity.ok(items);
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/account/{accountId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String accountId) {
        cartService.clearCart(accountId);
        return ResponseEntity.ok().build();
    }
}
