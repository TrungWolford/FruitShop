package server.FruitShop.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Cart.CartResponse;
import server.FruitShop.dto.response.ErrorResponse;
import server.FruitShop.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    // Admin endpoint - Get all carts with pagination
    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CartResponse>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CartResponse> carts = cartService.getAllCart(pageable);
            return ResponseEntity.ok(carts);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Cart endpoints - requires CUSTOMER role
    // Admin: Get cart by cartId
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable String cartId) {
        CartResponse cart = cartService.getCartById(cartId);
        if (cart != null) {
            return ResponseEntity.ok(cart);
        }
        return ResponseEntity.notFound().build();
    }

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
    public ResponseEntity<?> addCartItem(
            @PathVariable String accountId,
            @RequestBody CreateCartItemRequest request) {
        try {
            CartItemResponse cartItem = cartService.addCartItem(accountId, request);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartItemResponse cartItem = cartService.updateCartItem(cartItemId, request);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable String cartItemId) {
        try {
            cartService.removeCartItem(cartItemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
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

    // Admin endpoints - vô hiệu hóa giỏ hàng
    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{cartId}/disable")
    public ResponseEntity<CartResponse> disableCart(@PathVariable String cartId) {
        try {
            CartResponse cart = cartService.disableCart(cartId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{cartId}/enable")
    public ResponseEntity<CartResponse> enableCart(@PathVariable String cartId) {
        try {
            CartResponse cart = cartService.enableCart(cartId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{cartId}/status/{status}")
    public ResponseEntity<CartResponse> updateCartStatus(
            @PathVariable String cartId,
            @PathVariable int status) {
        try {
            CartResponse cart = cartService.updateCartStatus(cartId, status);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
