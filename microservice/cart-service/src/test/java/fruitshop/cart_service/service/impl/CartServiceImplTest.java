package fruitshop.cart_service.service.impl;

import fruitshop.cart_service.dto.request.CreateCartItemRequest;
import fruitshop.cart_service.dto.request.UpdateCartItemRequest;
import fruitshop.cart_service.entity.Cart;
import fruitshop.cart_service.entity.CartItem;
import fruitshop.cart_service.feign.AccountClient;
import fruitshop.cart_service.feign.ProductClient;
import fruitshop.cart_service.feign.dto.AccountSummaryDto;
import fruitshop.cart_service.feign.dto.ProductSummaryDto;
import fruitshop.cart_service.repository.CartItemRepository;
import fruitshop.cart_service.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private AccountClient accountClient;
    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addItem_existingItem_incrementsQuantity() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("acc-1");
        when(accountClient.getById("acc-1")).thenReturn(account);

        ProductSummaryDto product = new ProductSummaryDto();
        product.setProductId("p-1");
        when(productClient.getById("p-1")).thenReturn(product);

        Cart cart = new Cart();
        cart.setCartId("c-1");
        cart.setAccountId("acc-1");
        cart.setStatus(1);

        when(cartRepository.findByAccountId("acc-1")).thenReturn(Optional.of(cart));
        when(cartRepository.findById("c-1")).thenReturn(Optional.of(cart));

        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProductId("p-1");
        existing.setQuantity(2);
        when(cartItemRepository.findByCartCartIdAndProductId("c-1", "p-1")).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("p-1");
        request.setQuantity(3);

        cartService.addItem("acc-1", request);

        assertEquals(5, existing.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void updateItemQuantity_zeroQuantity_deletesItem() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("acc-1");
        when(accountClient.getById("acc-1")).thenReturn(account);

        Cart cart = new Cart();
        cart.setCartId("c-1");
        cart.setAccountId("acc-1");
        cart.setStatus(1);

        when(cartRepository.findByAccountId("acc-1")).thenReturn(Optional.of(cart));
        when(cartRepository.findById("c-1")).thenReturn(Optional.of(cart));

        CartItem item = new CartItem();
        item.setCartItemId("ci-1");
        item.setCart(cart);
        item.setProductId("p-1");
        item.setQuantity(2);
        when(cartItemRepository.findById("ci-1")).thenReturn(Optional.of(item));

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        cartService.updateItemQuantity("acc-1", "ci-1", request);

        verify(cartItemRepository).delete(item);
        verify(cartItemRepository, never()).save(item);
    }

    @Test
    void getOrCreateCart_newAccount_createsCart() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("acc-1");
        when(accountClient.getById("acc-1")).thenReturn(account);
        when(cartRepository.findByAccountId("acc-1")).thenReturn(Optional.empty());
        
        Cart newCart = new Cart();
        newCart.setCartId("c-1");
        newCart.setAccountId("acc-1");
        newCart.setStatus(1);
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        var response = cartService.getOrCreateCart("acc-1");
        
        assertEquals("c-1", response.getCartId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clearCart_removesAllItems() {
        Cart cart = new Cart();
        cart.setCartId("c-1");
        cart.setAccountId("acc-1");
        cart.setStatus(1);
        when(cartRepository.findByAccountId("acc-1")).thenReturn(Optional.of(cart));
        when(cartRepository.findById("c-1")).thenReturn(Optional.of(cart));
        
        cartService.clearCart("acc-1");
        
        verify(cartItemRepository).deleteByCartCartId("c-1");
    }

    @Test
    void removeItem_validItem_deletesFromCart() {
        AccountSummaryDto account = new AccountSummaryDto();
        account.setAccountId("acc-1");
        when(accountClient.getById("acc-1")).thenReturn(account);

        Cart cart = new Cart();
        cart.setCartId("c-1");
        cart.setAccountId("acc-1");
        cart.setStatus(1);
        when(cartRepository.findByAccountId("acc-1")).thenReturn(Optional.of(cart));
        when(cartRepository.findById("c-1")).thenReturn(Optional.of(cart));

        CartItem item = new CartItem();
        item.setCartItemId("ci-1");
        item.setCart(cart);
        when(cartItemRepository.findById("ci-1")).thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem("acc-1", "ci-1");
        
        verify(cartRepository).save(cart);
    }
}
