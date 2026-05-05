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
 * Cart Status: 0=Vô hiệu hóa, 1=Hoạt động
 * 
 * Cấu trúc test theo AAA Pattern:
 * - ARRANGE (Chuẩn bị): Thiết lập dữ liệu test và mock behavior
 * - ACT (Thực thi): Gọi method cần test
 * - ASSERT (Kiểm tra): Xác nhận kết quả đúng như mong đợi
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

    /**
     * Test 1: Lấy giỏ hàng theo ID
     * Mục đích: Kiểm tra getCartById() trả về đúng thông tin cart
     * Input: cartId hợp lệ "cart-001"
     */
    @Test
    @DisplayName("Test 1: Lấy cart theo ID - Thành công")
    void testGetCartById_Success() {
        // ARRANGE (Chuẩn bị): Giả lập repository trả về testCart khi gọi findById
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));

        // ACT (Thực thi): Gọi method getCartById cần test
        CartResponse result = cartService.getCartById("cart-001");

        // ASSERT (Kiểm tra): Xác nhận kết quả không null và cartId đúng
        assertNotNull(result);
        assertEquals("cart-001", result.getCartId());
        verify(cartRepository, times(1)).findById("cart-001"); // Kiểm tra repository được gọi đúng 1 lần
    }

    /**
     * Test 2: Lấy giỏ hàng của tài khoản
     * Mục đích: Kiểm tra getCartByAccountId() tìm cart theo accountId
     * Input: accountId hợp lệ "acc-001"
     */
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

    /**
     * Test 3: Tạo giỏ hàng mới
     * Mục đích: Kiểm tra createCart() tạo cart mới cho account
     * Input: accountId hợp lệ "acc-001"
     */
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

    /**
     * Test 4: Tạo cart với account không tồn tại
     * Mục đích: Kiểm tra createCart() throw exception khi accountId không hợp lệ
     * Input: accountId không tồn tại "invalid-id"
     */
    @Test
    @DisplayName("Test 4: Tạo cart - Account không tồn tại")
    void testCreateCart_AccountNotFound() {
        // ARRANGE (Chuẩn bị): Giả lập accountRepository trả về empty (không tìm thấy)
        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT (Thực thi + Kiểm tra): Gọi method và kiểm tra có throw exception không
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.createCart("invalid-id");
        });

        // ASSERT (Kiểm tra thêm): Xác nhận nội dung exception và repository không save
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).findById("invalid-id");
        verify(cartRepository, never()).save(any(Cart.class)); // never() = không được gọi
    }

    /**
     * Test 5: Tạo cart khi đã có cart
     * Mục đích: Kiểm tra createCart() trả về cart hiện tại thay vì tạo mới
     * Input: accountId đã có cart "acc-001"
     */
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

    /**
     * Test 6: Thêm sản phẩm vào giỏ hàng
     * Mục đích: Kiểm tra addCartItem() thêm product mới vào cart
     * Input: accountId, CreateCartItemRequest (productId, quantity=3)
     */
    @Test
    @DisplayName("Test 6: Thêm item vào cart - Thành công")
    void testAddCartItem_Success() {
        // ARRANGE (Chuẩn bị): Tạo request và giả lập các repository
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("prod-001");
        request.setQuantity(3);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart)); // Tìm thấy cart
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct)); // Tìm thấy product
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty()); // Chưa có trong cart
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem); // Lưu thành công

        // ACT (Thực thi): Gọi addCartItem để thêm product vào cart
        CartItemResponse result = cartService.addCartItem("acc-001", request);

        // ASSERT (Kiểm tra): Xác nhận kết quả không null và save được gọi
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test 7: Thêm sản phẩm đã có trong giỏ
     * Mục đích: Kiểm tra addCartItem() cộng thêm quantity khi product đã tồn tại
     * Input: productId đã có trong cart, quantity=3 (cộng thêm vào 2 hiện tại)
     */
    @Test
    @DisplayName("Test 7: Thêm item - Product đã có trong cart (update quantity)")
    void testAddCartItem_ProductExists() {
        // ARRANGE (Chuẩn bị): Product đã tồn tại trong cart với quantity=2
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("prod-001");
        request.setQuantity(3);

        when(cartRepository.findByAccountAccountId("acc-001")).thenReturn(Optional.of(testCart));
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem)); // Đã có trong cart
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // ACT (Thực thi): Thêm 3 sản phẩm nữa vào cart item hiện tại (2)
        CartItemResponse result = cartService.addCartItem("acc-001", request);

        // ASSERT (Kiểm tra): Quantity phải bằng 5 (2 + 3)
        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity()); // Kiểm tra quantity đã được cộng
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    /**
     * Test 8: Thêm sản phẩm không tồn tại
     * Mục đích: Kiểm tra addCartItem() throw exception khi productId không hợp lệ
     * Input: productId không tồn tại "invalid-id"
     */
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

    /**
     * Test 9: Cập nhật số lượng sản phẩm
     * Mục đích: Kiểm tra updateCartItem() thay đổi quantity của item
     * Input: cartItemId, UpdateCartItemRequest (quantity=5)
     */
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

    /**
     * Test 10: Cập nhật item không tồn tại
     * Mục đích: Kiểm tra updateCartItem() throw exception khi cartItemId không hợp lệ
     * Input: cartItemId không tồn tại "invalid-id"
     */
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

    /**
     * Test 11: Xóa sản phẩm khỏi giỏ hàng
     * Mục đích: Kiểm tra removeCartItem() xóa item khỏi cart
     * Input: cartItemId hợp lệ "item-001"
     */
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

    /**
     * Test 12: Vô hiệu hóa giỏ hàng
     * Mục đích: Kiểm tra disableCart() đổi status từ 1 sang 0
     * Input: cartId hợp lệ "cart-001"
     */
    @Test
    @DisplayName("Test 12: Disable cart - Thành công")
    void testDisableCart_Success() {
        // ARRANGE (Chuẩn bị): Giả lập tìm thấy cart và save thành công
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // ACT (Thực thi): Gọi disableCart để vô hiệu hóa
        CartResponse result = cartService.disableCart("cart-001");

        // ASSERT (Kiểm tra): Status phải đổi từ 1 sang 0
        assertNotNull(result);
        assertEquals(0, testCart.getStatus()); // Kiểm tra status = 0 (disabled)
        verify(cartRepository, times(1)).save(testCart);
    }

    /**
     * Test 13: Kích hoạt giỏ hàng
     * Mục đích: Kiểm tra enableCart() đổi status từ 0 sang 1
     * Input: cartId hợp lệ "cart-001" với status=0
     */
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

    /**
     * Test 14: Xóa toàn bộ giỏ hàng
     * Mục đích: Kiểm tra deleteCart() xóa cart và tất cả items
     * Input: cartId hợp lệ "cart-001" có chứa items
     */
    @Test
    @DisplayName("Test 14: Xóa toàn bộ cart - Thành công")
    void testDeleteCart_Success() {
        // ARRANGE (Chuẩn bị): Thêm items vào cart, giả lập các thao tác xóa
        List<CartItem> items = List.of(testCartItem);
        testCart.setItems(items);
        
        when(cartRepository.findById("cart-001")).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteAll(items); // doNothing() = giả lập method void
        doNothing().when(cartRepository).deleteById("cart-001");

        // ACT (Thực thi): Gọi deleteCart để xóa cart và tất cả items
        cartService.deleteCart("cart-001");

        // ASSERT (Kiểm tra): Xác nhận xóa items trước, rồi xóa cart
        verify(cartRepository, times(1)).findById("cart-001");
        verify(cartItemRepository, times(1)).deleteAll(items); // Xóa tất cả items trước
        verify(cartRepository, times(1)).deleteById("cart-001"); // Xóa cart sau
    }

    /**
     * Test 15: Thêm sản phẩm vào giỏ đã vô hiệu hóa
     * Mục đích: Kiểm tra addCartItem() throw exception khi cart có status=0
     * Input: accountId có cart bị disabled, CreateCartItemRequest
     */
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
