package fruitshop.cart_service.service.impl;

import fruitshop.cart_service.dto.request.CreateCartItemRequest;
import fruitshop.cart_service.dto.request.UpdateCartItemRequest;
import fruitshop.cart_service.dto.response.CartAccountResponse;
import fruitshop.cart_service.dto.response.CartItemResponse;
import fruitshop.cart_service.dto.response.CartResponse;
import fruitshop.cart_service.entity.Cart;
import fruitshop.cart_service.entity.CartItem;
import fruitshop.cart_service.exception.DownstreamServiceException;
import fruitshop.cart_service.exception.ResourceNotFoundException;
import fruitshop.cart_service.feign.AccountClient;
import fruitshop.cart_service.feign.ProductClient;
import fruitshop.cart_service.feign.dto.AccountSummaryDto;
import fruitshop.cart_service.feign.dto.ProductSummaryDto;
import fruitshop.cart_service.repository.CartItemRepository;
import fruitshop.cart_service.repository.CartRepository;
import fruitshop.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountClient accountClient;
    private final ProductClient productClient;

    @Override
    @Transactional
    public CartResponse getOrCreateCart(String accountId) {
        AccountSummaryDto accountDto = ensureAccountExists(accountId);

        Cart cart = cartRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAccountId(accountId);
                    newCart.setStatus(1);
                    return cartRepository.save(newCart);
                });
        return mapToResponse(cart, accountDto);
    }

    @Override
    @Transactional
    public CartResponse addItem(String accountId, CreateCartItemRequest request) {
        AccountSummaryDto accountDto = ensureAccountExists(accountId);
        ensureProductExists(request.getProductId());

        CartResponse currentCartDto = getOrCreateCart(accountId);
        Cart cart = cartRepository.findById(currentCartDto.getCartId()).orElseThrow();
        
        checkCartStatus(cart);
        CartItem item = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), request.getProductId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(request.getProductId());
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + Math.max(request.getQuantity(), 1));
        cartItemRepository.save(item);
        
        Cart updatedCart = cartRepository.findById(cart.getCartId()).orElse(cart);
        return mapToResponse(updatedCart, accountDto);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String accountId, String cartItemId, UpdateCartItemRequest request) {
        AccountSummaryDto accountDto = ensureAccountExists(accountId);

        CartResponse currentCartDto = getOrCreateCart(accountId);
        Cart cart = cartRepository.findById(currentCartDto.getCartId()).orElseThrow();
        
        checkCartStatus(cart);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Cart item does not belong to account");
        }

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        Cart updatedCart = cartRepository.findById(cart.getCartId()).orElse(cart);
        return mapToResponse(updatedCart, accountDto);
    }
    @Override
    @Transactional
    public CartResponse updateItemQuantityByItemId(String cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        Cart cart = item.getCart();
        checkCartStatus(cart);

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        Cart updatedCart = cartRepository.findById(cart.getCartId()).orElse(cart);
        
        AccountSummaryDto accountDto = null;
        try {
            accountDto = accountClient.getById(cart.getAccountId());
        } catch (Exception ignored) {}

        return mapToResponse(updatedCart, accountDto);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String accountId, String cartItemId) {
        AccountSummaryDto accountDto = ensureAccountExists(accountId);

        CartResponse currentCartDto = getOrCreateCart(accountId);
        Cart cart = cartRepository.findById(currentCartDto.getCartId()).orElseThrow();
        
        checkCartStatus(cart);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Cart item does not belong to account");
        }

        cart.getItems().remove(item);
        Cart updatedCart = cartRepository.save(cart);
        return mapToResponse(updatedCart, accountDto);
    }
    @Override
    @Transactional
    public CartResponse removeItemByItemId(String cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        Cart cart = item.getCart();
        checkCartStatus(cart);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        
        Cart updatedCart = cartRepository.findById(cart.getCartId()).orElse(cart);
        
        AccountSummaryDto accountDto = null;
        try {
            accountDto = accountClient.getById(cart.getAccountId());
        } catch (Exception ignored) {}

        return mapToResponse(updatedCart, accountDto);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(String cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));
        
        checkCartStatus(item.getCart());
        
        if (request.getQuantity() <= 0) {
            Cart cart = item.getCart();
            cart.getItems().remove(item);
            cartRepository.save(cart);
            return null;
        } else {
            item.setQuantity(request.getQuantity());
            return mapToItemResponse(cartItemRepository.save(item));
        }
    }

    @Override
    @Transactional
    public void removeCartItem(String cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));
        checkCartStatus(item.getCart());
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .build();
    }

    @Override
    @Transactional
    public CartResponse clearCart(String accountId) {
        Cart cart = cartRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for account: " + accountId));
        
        checkCartStatus(cart);
        cartItemRepository.deleteByCartCartId(cart.getCartId());
        
        // Refetch to get clean state
        Cart savedCart = cartRepository.findById(cart.getCartId()).orElse(cart);

        AccountSummaryDto accountDto = null;
        try {
            accountDto = accountClient.getById(accountId);
        } catch (Exception ignored) {}

        return mapToResponse(savedCart, accountDto);
    }

    private void checkCartStatus(Cart cart) {
        if (cart.getStatus() != 1) {
            throw new IllegalArgumentException("Giỏ hàng đã bị vô hiệu hóa do vi phạm chính sách, vui lòng liên hệ VuaTraiCay để biết thêm chi tiết");
        }
    }

    @Override
    public List<CartItemResponse> getCartItemsByAccountId(String accountId) {
        CartResponse cart = getOrCreateCart(accountId);
        return cart.getItems();
    }

    @Override
    @Transactional
    public CartResponse updateCartStatus(String cartId, int status) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));
        cart.setStatus(status);
        Cart savedCart = cartRepository.save(cart);
        
        AccountSummaryDto accountDto = null;
        try {
            accountDto = accountClient.getById(cart.getAccountId());
        } catch (Exception ignored) {}
        
        return mapToResponse(savedCart, accountDto);
    }

    @Override
    @Transactional
    public CartResponse enableCart(String cartId) {
        return updateCartStatus(cartId, 1);
    }

    @Override
    @Transactional
    public CartResponse disableCart(String cartId) {
        return updateCartStatus(cartId, 0);
    }

    private AccountSummaryDto ensureAccountExists(String accountId) {
        try {
            AccountSummaryDto account = accountClient.getById(accountId);
            if (account == null || account.getAccountId() == null) {
                throw new ResourceNotFoundException("Account not found with id: " + accountId);
            }
            return account;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Account service unavailable while validating accountId: " + accountId);
        }
    }

    private ProductSummaryDto ensureProductExists(String productId) {
        try {
            ProductSummaryDto product = productClient.getById(productId);
            if (product == null || product.getProductId() == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
            return product;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Catalog service unavailable while validating productId: " + productId);
        }
    }

    private CartResponse mapToResponse(Cart cart, AccountSummaryDto accountDto) {
        if (cart == null) return null;

        List<CartItemResponse> itemResponses = new ArrayList<>();
        long totalPrice = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                CartItemResponse itemDto = CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build();

                try {
                    ProductSummaryDto product = productClient.getById(item.getProductId());
                    if (product != null) {
                        itemDto.setProductName(product.getProductName());
                        itemDto.setProductPrice(product.getPrice());
                        itemDto.setTotalPrice(product.getPrice() * item.getQuantity());
                        itemDto.setImages(product.getImages());
                        
                        totalPrice += itemDto.getTotalPrice();
                    }
                } catch (Exception e) {
                    itemDto.setProductName("Unknown Product");
                }
                itemResponses.add(itemDto);
            }
        }

        CartResponse response = CartResponse.builder()
                .cartId(cart.getCartId())
                .accountId(cart.getAccountId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .totalItems(itemResponses.size())
                .createdAt(cart.getCreatedAt())
                .status(cart.getStatus())
                .statusText(cart.getStatus() == 1 ? "Hoạt động" : "Khóa")
                .build();

        if (accountDto != null) {
            response.setAccountName(accountDto.getAccountName());
            response.setAccount(CartAccountResponse.builder()
                    .accountId(accountDto.getAccountId())
                    .accountName(accountDto.getAccountName())
                    .accountPhone(accountDto.getAccountPhone())
                    .status(accountDto.getStatus())
                    .build());
        }

        return response;
    }

    @Override
    public Page<CartResponse> getAllCart(Pageable pageable) {
        return cartRepository.findAll(pageable).map(cart -> {
            AccountSummaryDto accountDto = null;
            try {
                accountDto = accountClient.getById(cart.getAccountId());
            } catch (Exception ignored) {}
            return mapToResponse(cart, accountDto);
        });
    }

    @Override
    public CartResponse getCartById(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));
        
        AccountSummaryDto accountDto = null;
        try {
            accountDto = accountClient.getById(cart.getAccountId());
        } catch (Exception ignored) {}
        
        return mapToResponse(cart, accountDto);
    }

    @Override
    @Transactional
    public void deleteCart(String cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new IllegalArgumentException("Cart not found: " + cartId);
        }
        cartRepository.deleteById(cartId);
    }
}
