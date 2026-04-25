package fruitshop.cart_service.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountClient accountClient;
    private final ProductClient productClient;

    @Override
    @Transactional
    public Cart getOrCreateCart(String accountId) {
        ensureAccountExists(accountId);

        return cartRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setAccountId(accountId);
                    cart.setStatus(1);
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public Cart addItem(String accountId, String productId, int quantity) {
        ensureAccountExists(accountId);
        ensureProductExists(productId);

        Cart cart = getOrCreateCart(accountId);
        CartItem item = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(productId);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + Math.max(quantity, 1));
        cartItemRepository.save(item);
        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    @Override
    @Transactional
    public Cart updateItemQuantity(String accountId, String cartItemId, int quantity) {
        ensureAccountExists(accountId);

        Cart cart = getOrCreateCart(accountId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Cart item does not belong to account");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    @Override
    @Transactional
    public Cart removeItem(String accountId, String cartItemId) {
        ensureAccountExists(accountId);

        Cart cart = getOrCreateCart(accountId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Cart item does not belong to account");
        }

        cartItemRepository.delete(item);
        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    @Override
    @Transactional
    public Cart clearCart(String accountId) {
        ensureAccountExists(accountId);

        Cart cart = getOrCreateCart(accountId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    private void ensureAccountExists(String accountId) {
        try {
            AccountSummaryDto account = accountClient.getById(accountId);
            if (account == null || account.getAccountId() == null) {
                throw new ResourceNotFoundException("Account not found with id: " + accountId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Account service unavailable while validating accountId: " + accountId);
        }
    }

    private void ensureProductExists(String productId) {
        try {
            ProductSummaryDto product = productClient.getById(productId);
            if (product == null || product.getProductId() == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DownstreamServiceException("Catalog service unavailable while validating productId: " + productId);
        }
    }
}
