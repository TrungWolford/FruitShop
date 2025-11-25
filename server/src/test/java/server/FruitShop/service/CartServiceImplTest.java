package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import server.FruitShop.service.Impl.CartServiceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CartService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Cart Service")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Account testAccount;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Test User");

        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài");
        testProduct.setPrice(50000);

        testCart = new Cart();
        testCart.setCartId("cart-001");
        testCart.setAccount(testAccount);
        testCart.setStatus(1);
        testCart.setCreatedAt(new Date());
        testCart.setItems(new ArrayList<>());

        testCartItem = new CartItem();
        testCartItem.setCartItemId("item-001");
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
    }

    @Test
    @DisplayName("Test 1: Lấy cart theo ID - Thành công")
    void testGetCartById_Success() {
        // ARRANGE
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));

        // ACT
        CartResponse result = cartService.getCartById("cart-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("cart-001", result.getCartId());
        verify(cartRepository, times(1)).findById("cart-001");
    }

    @Test
    @DisplayName("Test 2: Lấy cart theo account ID - Thành công")
    void testGetCartByAccountId_Success() {
        // ARRANGE
        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));

        // ACT
        CartResponse result = cartService.getCartByAccountId("acc-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("cart-001", result.getCartId());
        verify(cartRepository, times(1)).findByAccountAccountId("acc-001");
    }

    @Test
    @DisplayName("Test 3: Tạo cart mới - Thành công")
    void testCreateCart_Success() {
        // ARRANGE
        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // ACT
        CartResponse result = cartService.createCart("acc-001");

        // ASSERT
        assertNotNull(result);
        verify(accountRepository, times(1)).findById("acc-001");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Test 4: Tạo cart - Account không tồn tại")
    void testCreateCart_AccountNotFound() {
        // ARRANGE
        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.createCart("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).findById("invalid-id");
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Test 5: Tạo cart - Cart đã tồn tại")
    void testCreateCart_AlreadyExists() {
        // ARRANGE
        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));

        // ACT
        CartResponse result = cartService.createCart("acc-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("cart-001", result.getCartId());
        verify(cartRepository, never()).save(any(Cart.class)); // Không tạo mới
    }

    @Test
    @DisplayName("Test 6: Thêm item vào cart - Thành công")
    void testAddCartItem_Success() {
        // ARRANGE
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("prod-001");
        request.setQuantity(3);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // ACT
        CartItemResponse result = cartService.addCartItem("acc-001", request);

        // ASSERT
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Test 7: Thêm item - Product đã có trong cart (update quantity)")
    void testAddCartItem_ProductExists() {
        // ARRANGE
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("prod-001");
        request.setQuantity(3);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // ACT
        CartItemResponse result = cartService.addCartItem("acc-001", request);

        // ASSERT
        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    @Test
    @DisplayName("Test 8: Thêm item - Product không tồn tại")
    void testAddCartItem_ProductNotFound() {
        // ARRANGE
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("invalid-id");
        request.setQuantity(1);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addCartItem("acc-001", request);
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Test 9: Cập nhật cart item - Thành công")
    void testUpdateCartItem_Success() {
        // ARRANGE
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartItemRepository.findById("item-001")).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // ACT
        CartItemResponse result = cartService.updateCartItem("item-001", request);

        // ASSERT
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    @Test
    @DisplayName("Test 10: Cập nhật cart item - Không tìm thấy")
    void testUpdateCartItem_NotFound() {
        // ARRANGE
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartItemRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItem("invalid-id", request);
        });

        assertTrue(exception.getMessage().contains("Cart item not found"));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Test 11: Xóa cart item - Thành công")
    void testRemoveCartItem_Success() {
        // ARRANGE
        when(cartItemRepository.findById("item-001")).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).deleteById("item-001");

        // ACT
        cartService.removeCartItem("item-001");

        // ASSERT
        verify(cartItemRepository, times(1)).findById("item-001");
        verify(cartItemRepository, times(1)).deleteById("item-001");
    }

    @Test
    @DisplayName("Test 12: Disable cart - Thành công")
    void testDisableCart_Success() {
        // ARRANGE
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // ACT
        CartResponse result = cartService.disableCart("cart-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(0, testCart.getStatus());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    @DisplayName("Test 13: Enable cart - Thành công")
    void testEnableCart_Success() {
        // ARRANGE
        testCart.setStatus(0); // Disabled
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // ACT
        CartResponse result = cartService.enableCart("cart-001");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, testCart.getStatus());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    @DisplayName("Test 14: Xóa toàn bộ cart - Thành công")
    void testDeleteCart_Success() {
        // ARRANGE
        List<CartItem> items = List.of(testCartItem);
        testCart.setItems(items);
        
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteAll(items);
        doNothing().when(cartRepository).deleteById("cart-001");

        // ACT
        cartService.deleteCart("cart-001");

        // ASSERT
        verify(cartRepository, times(1)).findById("cart-001");
        verify(cartItemRepository, times(1)).deleteAll(items);
        verify(cartRepository, times(1)).deleteById("cart-001");
    }

    @Test
    @DisplayName("Test 15: Thêm item vào cart bị disabled - Thất bại")
    void testAddCartItem_CartDisabled() {
        // ARRANGE
        testCart.setStatus(0); // Disabled
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("prod-001");
        request.setQuantity(1);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addCartItem("acc-001", request);
        });

        assertTrue(exception.getMessage().contains("vô hiệu hóa"));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }
}
