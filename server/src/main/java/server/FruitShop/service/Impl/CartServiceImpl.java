package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import server.FruitShop.dto.response.Cart.CartItemResponse;
import server.FruitShop.dto.response.Cart.CartResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Cart;
import server.FruitShop.entity.CartItem;
import server.FruitShop.entity.Product;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.CartItemRepository;
import server.FruitShop.repository.CartRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.service.CartService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public CartResponse getCartByAccountId(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        return cartOptional.map(cart -> CartResponse.fromEntity(cart)).orElse(null);
    }

    @Override
    public CartResponse createCart(String accountId) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        // Check if cart already exists
        Optional<Cart> existingCart = cartRepository.findByAccountAccountId(accountId);
        if (existingCart.isPresent()) {
            return CartResponse.fromEntity(existingCart.get());
        }

        Cart cart = new Cart();
        cart.setAccount(accountOptional.get());
        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    public void deleteCart(String cartId) {
        Optional<Cart> cartOptional = cartRepository.findById(cartId);
        if (cartOptional.isPresent()) {
            // Delete all cart items first
            List<CartItem> items = cartOptional.get().getItems();
            cartItemRepository.deleteAll(items);
            // Then delete the cart
            cartRepository.deleteById(cartId);
        }
    }

    @Override
    public CartItemResponse addCartItem(String accountId, CreateCartItemRequest request) {
        // Get or create cart for account
        Cart cart = getOrCreateCart(accountId);

        // Check if product exists
        Optional<Product> productOptional = productRepository.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + request.getProductId());
        }

        Product product = productOptional.get();

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            CartItem savedItem = cartItemRepository.save(item);
            return CartItemResponse.fromEntity(savedItem);
        }

        // Create new cart item
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());

        CartItem savedItem = cartItemRepository.save(cartItem);
        return CartItemResponse.fromEntity(savedItem);
    }

    @Override
    public CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest request) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isEmpty()) {
            throw new RuntimeException("Cart item not found with id: " + cartItemId);
        }

        CartItem cartItem = cartItemOptional.get();
        cartItem.setQuantity(request.getQuantity());

        CartItem savedItem = cartItemRepository.save(cartItem);
        return CartItemResponse.fromEntity(savedItem);
    }

    @Override
    public void removeCartItem(String cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isPresent()) {
            cartItemRepository.deleteById(cartItemId);
        }
    }

    @Override
    public List<CartItemResponse> getCartItemsByAccountId(String accountId) {
        System.out.println("📦 GetCartItemsByAccountId called for: " + accountId);
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        if (cartOptional.isPresent()) {
            List<CartItem> items = cartOptional.get().getItems();
            System.out.println("📦 Found cart with " + items.size() + " items");
            for (CartItem item : items) {
                System.out.println("📦 Cart item: " + item.getCartItemId() + " - Product: " + item.getProduct().getProductId() + " - Quantity: " + item.getQuantity());
            }
            return items.stream()
                    .map(CartItemResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        System.out.println("📦 No cart found for accountId: " + accountId);
        return List.of();
    }

    @Override
    @Transactional
    public void clearCart(String accountId) {
        System.out.println("🧹 ClearCart called for accountId: " + accountId);
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            String cartId = cart.getCartId();
            System.out.println("🧹 Found cart with ID: " + cartId);

            // Delete all cart items for this cart using direct repository query
            List<CartItem> itemsToDelete = cartItemRepository.findByCartCartId(cartId);
            System.out.println("🧹 Found " + itemsToDelete.size() + " items to delete using repository query");

            // Log each item before deletion
            for (CartItem item : itemsToDelete) {
                System.out.println("🧹 Deleting cart item: " + item.getCartItemId() + " - Product: " + item.getProduct().getProductId());
            }

            // Try JPQL delete first (more efficient)
            cartItemRepository.deleteByCartId(cartId);

            // Alternative: Delete using repository
            // cartItemRepository.deleteAll(itemsToDelete);

            System.out.println("🧹 Cart cleared successfully for accountId: " + accountId);

            // Verify deletion
            List<CartItem> remainingItems = cartItemRepository.findByCartCartId(cartId);
            System.out.println("🧹 Verification: " + remainingItems.size() + " items remaining after deletion");
        } else {
            System.out.println("🧹 No cart found for accountId: " + accountId);
        }
    }

    private Cart getOrCreateCart(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }

        // Create new cart
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        Cart cart = new Cart();
        cart.setAccount(accountOptional.get());
        return cartRepository.save(cart);
    }
}
