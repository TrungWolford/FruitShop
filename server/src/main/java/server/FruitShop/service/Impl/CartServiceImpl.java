package server.FruitShop.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
import server.FruitShop.exception.ResourceNotFoundException;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CartServiceImpl — cache strategy: Redis only (L2).
 *
 * Lý do không dùng Caffeine cho Cart:
 *   - Giỏ hàng là dữ liệu per-user, thay đổi thường xuyên (thêm/bớt/xóa item).
 *   - Nếu dùng Caffeine (in-process), dữ liệu không đồng bộ giữa nhiều instance
 *     khi scale horizontal.
 *   - Redis đảm bảo consistency across instances.
 *
 * Cache key convention:
 *   getCartByAccountId:  "fruitshop:cart:account:{accountId}"   TTL 30 phút
 *   getCartItemsByAccountId: "fruitshop:cart:items:{accountId}" TTL 30 phút
 *
 * Eviction: mọi write operation sẽ xóa cache của accountId liên quan.
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX  = "fruitshop:cart:account:";
    private static final String ITEMS_KEY_PREFIX = "fruitshop:cart:items:";
    private static final Duration CART_TTL       = Duration.ofMinutes(30);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // =========================================================================
    // READ
    // =========================================================================

    @Override
    public Page<CartResponse> getAllCart(Pageable pageable) {
        Page<Cart> cartPage = cartRepository.findAll(pageable);
        return cartPage.map(CartResponse::fromEntity);
    }

    @Override
    public CartResponse getCartById(String cartId) {
        try {
            Optional<Cart> cartOptional = cartRepository.findById(cartId);
            return cartOptional.map(CartResponse::fromEntity).orElse(null);
        } catch (Exception e) {
            log.error("Error fetching cart for cartId {}: {}", cartId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy cart theo accountId — Redis cache.
     * Key: "fruitshop:cart:account:{accountId}"
     */
    @Override
    @SuppressWarnings("unchecked")
    public CartResponse getCartByAccountId(String accountId) {
        String key = CART_KEY_PREFIX + accountId;

        // L2: Redis
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof CartResponse cached) {
            log.debug("[Cart Cache HIT] account:{}", accountId);
            return cached;
        }

        // DB
        log.debug("[Cart Cache MISS] account:{} → query DB", accountId);
        try {
            Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
            CartResponse response = cartOptional.map(CartResponse::fromEntity).orElse(null);

            if (response != null) {
                redisTemplate.opsForValue().set(key, response, CART_TTL);
                log.debug("[Cart Cache PUT] account:{}", accountId);
            }

            return response;
        } catch (Exception e) {
            log.error("Error fetching cart for accountId {}: {}", accountId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy cart items theo accountId — Redis cache.
     * Key: "fruitshop:cart:items:{accountId}"
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<CartItemResponse> getCartItemsByAccountId(String accountId) {
        String key = ITEMS_KEY_PREFIX + accountId;

        // L2: Redis
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof List<?> list) {
            log.debug("[CartItems Cache HIT] account:{} items={}", accountId, list.size());
            return (List<CartItemResponse>) list;
        }

        // DB
        log.debug("[CartItems Cache MISS] account:{} → query DB", accountId);
        try {
            List<CartItemResponse> items = cartRepository.findByAccountAccountId(accountId)
                    .map(cart -> {
                        List<CartItem> cartItems = cartItemRepository.findByCartCartId(cart.getCartId());
                        return cartItems.stream()
                                .map(CartItemResponse::fromEntity)
                                .collect(Collectors.toList());
                    })
                    .orElse(List.of());

            if (!items.isEmpty()) {
                redisTemplate.opsForValue().set(key, items, CART_TTL);
                log.debug("[CartItems Cache PUT] account:{} items={}", accountId, items.size());
            }

            return items;
        } catch (Exception e) {
            log.error("Error fetching cart items for accountId {}: {}", accountId, e.getMessage(), e);
            return List.of();
        }
    }

    // =========================================================================
    // WRITE — evict Redis cache sau mỗi thao tác ghi
    // =========================================================================

    @Override
    public CartResponse createCart(String accountId) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }

        Optional<Cart> existingCart = cartRepository.findByAccountAccountId(accountId);
        if (existingCart.isPresent()) {
            return CartResponse.fromEntity(existingCart.get());
        }

        Cart cart = new Cart();
        cart.setAccount(accountOptional.get());
        cart.setCreatedAt(new Date());
        cart.setStatus(1);
        Cart savedCart = cartRepository.save(cart);

        evictCartCache(accountId);
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    public void deleteCart(String cartId) {
        Optional<Cart> cartOptional = cartRepository.findById(cartId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            String accountId = cart.getAccount() != null ? cart.getAccount().getAccountId() : null;

            List<CartItem> items = cart.getItems();
            cartItemRepository.deleteAll(items);
            cartRepository.deleteById(cartId);

            if (accountId != null) evictCartCache(accountId);
        }
    }

    @Override
    public CartItemResponse addCartItem(String accountId, CreateCartItemRequest request) {
        Cart cart = getOrCreateCart(accountId);

        if (cart.getStatus() != 1) {
            throw new RuntimeException("Giỏ hàng đã bị vô hiệu hóa do vi phạm chính sách, vui lòng liên hệ VuaTraiCay để biết thêm chi tiết");
        }

        Optional<Product> productOptional = productRepository.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }

        Product product = productOptional.get();

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        CartItem savedItem;
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            savedItem = cartItemRepository.save(item);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            savedItem = cartItemRepository.save(cartItem);
        }

        // Evict cache vì cart đã thay đổi
        evictCartCache(accountId);
        return CartItemResponse.fromEntity(savedItem);
    }

    @Override
    public CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest request) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }

        CartItem cartItem = cartItemOptional.get();

        Cart cart = cartItem.getCart();
        if (cart != null && cart.getStatus() != 1) {
            throw new RuntimeException("Giỏ hàng đã bị vô hiệu hóa do vi phạm chính sách, vui lòng liên hệ VuaTraiCay để biết thêm chi tiết");
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem savedItem = cartItemRepository.save(cartItem);

        // Evict cache
        if (cart != null && cart.getAccount() != null) {
            evictCartCache(cart.getAccount().getAccountId());
        }

        return CartItemResponse.fromEntity(savedItem);
    }

    @Override
    public void removeCartItem(String cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();

            Cart cart = cartItem.getCart();
            if (cart != null && cart.getStatus() != 1) {
                throw new RuntimeException("Giỏ hàng đã bị vô hiệu hóa do vi phạm chính sách, vui lòng liên hệ VuaTraiCay để biết thêm chi tiết");
            }

            cartItemRepository.deleteById(cartItemId);

            // Evict cache
            if (cart != null && cart.getAccount() != null) {
                evictCartCache(cart.getAccount().getAccountId());
            }
        }
    }

    @Override
    @Transactional
    public void clearCart(String accountId) {
        log.debug("clearCart accountId:{}", accountId);
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        if (cartOptional.isPresent()) {
            cartItemRepository.deleteByCartId(cartOptional.get().getCartId());
            evictCartCache(accountId);
            log.debug("Cart cleared for accountId:{}", accountId);
        }
    }

    @Override
    public CartResponse disableCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        cart.setStatus(0);
        Cart savedCart = cartRepository.save(cart);
        if (cart.getAccount() != null) evictCartCache(cart.getAccount().getAccountId());
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    public CartResponse enableCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        cart.setStatus(1);
        Cart savedCart = cartRepository.save(cart);
        if (cart.getAccount() != null) evictCartCache(cart.getAccount().getAccountId());
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    public CartResponse updateCartStatus(String cartId, int status) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        cart.setStatus(status);
        Cart savedCart = cartRepository.save(cart);
        if (cart.getAccount() != null) evictCartCache(cart.getAccount().getAccountId());
        return CartResponse.fromEntity(savedCart);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Cart getOrCreateCart(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountAccountId(accountId);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }

        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }

        Cart cart = new Cart();
        cart.setAccount(accountOptional.get());
        cart.setCreatedAt(new Date());
        cart.setStatus(1);
        return cartRepository.save(cart);
    }

    /**
     * Xóa cả cart và cart-items cache của một accountId trên Redis.
     */
    private void evictCartCache(String accountId) {
        redisTemplate.delete(CART_KEY_PREFIX + accountId);
        redisTemplate.delete(ITEMS_KEY_PREFIX + accountId);
        log.debug("[Cart Cache EVICT] account:{}", accountId);
    }
}
