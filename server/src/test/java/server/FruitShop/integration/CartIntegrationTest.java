package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Cart.CreateCartItemRequest;
import server.FruitShop.dto.request.Cart.UpdateCartItemRequest;
import java.util.Set;
import java.util.HashSet;
import server.FruitShop.entity.*;
import server.FruitShop.repository.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Cart API
 * Test toàn bộ flow: Controller → Service → Repository → Database
 * Bao gồm:
 * - Quản lý cart (tạo, xem, xóa, enable/disable)
 * - Quản lý cart items (thêm, sửa, xóa)
 * - Xử lý các trường hợp lỗi (product không tồn tại, cart disabled)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Cart testCart;
    private CartItem testCartItem;
    private Account testAccount;
    private Product testProduct;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo account với role mới để tránh shared reference
        testAccount = new Account();
        testAccount.setAccountName("Nguyễn Văn A");
        testAccount.setAccountPhone("0355142890");
        testAccount.setPassword(passwordEncoder.encode("123456"));
        testAccount.setStatus(1);
        
        // Tạo role mới cho mỗi account (tránh share cùng 1 Role instance)
        Role customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");
        customerRole = roleRepository.save(customerRole);
        
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        testAccount.setRoles(roles);
        testAccount = accountRepository.save(testAccount);

        // Tạo category
        Category testCategory = new Category();
        testCategory.setCategoryName("Trái cây");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);

        // Tạo product
        testProduct = new Product();
        testProduct.setProductName("Xoài Úc");
        testProduct.setPrice(50000);
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct.setCategories(Collections.singletonList(testCategory));
        testProduct = productRepository.save(testProduct);

        // Tạo cart
        testCart = new Cart();
        testCart.setAccount(testAccount);
        testCart.setStatus(1);
        testCart = cartRepository.save(testCart);

        // Tạo cart item
        testCartItem = new CartItem();
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem = cartItemRepository.save(testCartItem);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả giỏ hàng với phân trang
     * Mục đích: Kiểm tra API GET /api/cart trả về danh sách carts
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response có ít nhất 1 cart (testCart từ setUp)
     * - Mỗi cart có đầy đủ thông tin status
     */
    @Test
    @DisplayName("Integration Test 1: Lấy tất cả carts - Thành công")
    void testGetAllCarts_Success() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status").exists());
    }

    /**
     * Test Case 2: Lấy thông tin giỏ hàng theo ID
     * Mục đích: Kiểm tra API GET /api/cart/{id} trả về đúng thông tin cart
     * Input: ID của testCart đã tạo trong setUp
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response chứa cartId và status khớp với dữ liệu test
     */
    @Test
    @DisplayName("Integration Test 2: Lấy cart theo ID - Thành công")
    void testGetCartById_Success() throws Exception {
        mockMvc.perform(get("/api/cart/{id}", testCart.getCartId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test Case 3: Lấy cart với ID không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi query cart với ID không hợp lệ
     * Input: ID không tồn tại ("invalid-cart-id")
     * Kết quả mong muốn:
     * - HTTP Status: 404 Not Found
     * - Hệ thống xử lý exception đúng cách
     */
    @Test
    @DisplayName("Integration Test 3: Lấy cart theo ID - Không tồn tại")
    void testGetCartById_NotFound() throws Exception {
        mockMvc.perform(get("/api/cart/{id}", "invalid-cart-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 4: Lấy giỏ hàng theo tài khoản
     * Mục đích: Kiểm tra API GET /api/cart/account/{accountId}
     * Input: accountId của testAccount
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response chứa cart với đúng accountId
     * Use case: Khách hàng xem giỏ hàng của mình
     */
    @Test
    @DisplayName("Integration Test 4: Lấy cart theo accountId - Thành công")
    void testGetCartByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/cart/account/{accountId}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId").value(testAccount.getAccountId()));
    }

    /**
     * Test Case 5: Tạo giỏ hàng mới cho tài khoản
     * Mục đích: Kiểm tra API POST /api/cart/account/{accountId}
     * Input: Account mới được tạo với role CUSTOMER
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Cart mới được tạo và liên kết với account
     * - Database có 2 carts (testCart ban đầu + cart mới)
     * Business logic: Mỗi account có 1 cart riêng
     */
    @Test
    @DisplayName("Integration Test 5: Tạo cart mới - Thành công")
    void testCreateCart_Success() throws Exception {
        // Tạo account mới với role mới (tránh shared reference)
        Role newRole = new Role();
        newRole.setRoleName("CUSTOMER");
        newRole = roleRepository.save(newRole);
        
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(newRole);
        
        Account newAccount = new Account();
        newAccount.setAccountName("Trần Thị B");
        newAccount.setAccountPhone("0999999999");
        newAccount.setPassword(passwordEncoder.encode("123456"));
        newAccount.setStatus(1);
        newAccount.setRoles(newRoles);
        newAccount = accountRepository.save(newAccount);

        mockMvc.perform(post("/api/cart/account/{accountId}", newAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId").value(newAccount.getAccountId()));

        // Verify trong database
        long count = cartRepository.count();
        assert count == 2;
    }

    /**
     * Test Case 6: Xóa giỏ hàng
     * Mục đích: Kiểm tra API DELETE /api/cart/{id}
     * Setup: Xóa cart items trước để tránh foreign key constraint
     * Input: ID của testCart
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Cart bị xóa khỏi database (hard delete)
     */
    @Test
    @DisplayName("Integration Test 6: Xóa cart - Thành công")
    void testDeleteCart_Success() throws Exception {
        // Xóa cart items trước
        cartItemRepository.deleteAll();
        
        mockMvc.perform(delete("/api/cart/{id}", testCart.getCartId()))
                .andExpect(status().isOk());

        // Verify trong database
        boolean exists = cartRepository.existsById(testCart.getCartId());
        assert !exists;
    }

    /**
     * Test Case 7: Thêm sản phẩm vào giỏ hàng
     * Mục đích: Kiểm tra API POST /api/cart/account/{accountId}/items
     * Input: CreateCartItemRequest
     * - productId: Táo Fuji (product mới tạo)
     * - quantity: 3
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response chứa: quantity=3, productId, productName, productPrice
     * - Database có 2 cart items (testCartItem + item mới)
     * Use case: Khách hàng thêm sản phẩm vào giỏ
     */
    @Test
    @DisplayName("Integration Test 7: Thêm cart item - Thành công")
    void testAddCartItem_Success() throws Exception {
        // Tạo product mới
        Product newProduct = new Product();
        newProduct.setProductName("Táo Fuji");
        newProduct.setPrice(60000);
        newProduct.setStock(50);
        newProduct.setStatus(1);
        // Tạo mới ArrayList để tránh shared reference
        newProduct.setCategories(new java.util.ArrayList<>(testProduct.getCategories()));
        newProduct = productRepository.save(newProduct);

        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId(newProduct.getProductId());
        request.setQuantity(3);

        mockMvc.perform(post("/api/cart/account/{accountId}/items", testAccount.getAccountId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.productId").value(newProduct.getProductId()))
                .andExpect(jsonPath("$.productName").value("Táo Fuji"))
                .andExpect(jsonPath("$.productPrice").value(60000));

        // Verify trong database
        long count = cartItemRepository.count();
        assert count == 2;
    }

    /**
     * Test Case 8: Cập nhật số lượng sản phẩm trong giỏ
     * Mục đích: Kiểm tra API PUT /api/cart/items/{cartItemId}
     * Input: UpdateCartItemRequest với quantity = 5
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response quantity = 5
     * - Database: cart item được cập nhật số lượng mới
     * Use case: Khách hàng thay đổi số lượng sản phẩm trong giỏ
     */
    @Test
    @DisplayName("Integration Test 8: Cập nhật cart item - Thành công")
    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/cart/items/{cartItemId}", testCartItem.getCartItemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        // Verify trong database
        CartItem updated = cartItemRepository.findById(testCartItem.getCartItemId()).orElseThrow();
        assert updated.getQuantity() == 5;
    }

    /**
     * Test Case 9: Xóa sản phẩm khỏi giỏ hàng
     * Mục đích: Kiểm tra API DELETE /api/cart/items/{cartItemId}
     * Input: ID của testCartItem
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Cart item bị xóa khỏi database
     * Use case: Khách hàng bỏ sản phẩm ra khỏi giỏ
     */
    @Test
    @DisplayName("Integration Test 9: Xóa cart item - Thành công")
    void testRemoveCartItem_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", testCartItem.getCartItemId()))
                .andExpect(status().isOk());

        // Verify trong database
        boolean exists = cartItemRepository.existsById(testCartItem.getCartItemId());
        assert !exists;
    }

    /**
     * Test Case 10: Lấy danh sách sản phẩm trong giỏ theo tài khoản
     * Mục đích: Kiểm tra API GET /api/cart/account/{accountId}/items
     * Setup: @Commit để đảm bảo transaction được commit, data có sẵn
     * Input: accountId của testAccount
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response là array có ít nhất 1 item
     * - Item đầu tiên có productId khớp với testProduct
     * Use case: Hiển thị danh sách sản phẩm trong giỏ hàng
     */
    @Test
    @DisplayName("Integration Test 10: Lấy cart items theo accountId - Thành công")
    @Commit // Đảm bảo transaction được commit để data có sẵn
    void testGetCartItemsByAccountId_Success() throws Exception {
        // Clear và flush để ensure data được commit
        cartItemRepository.flush();
        cartRepository.flush();
        
        // Verify cart item tồn tại trong database trước
        List<CartItem> itemsInDb = cartItemRepository.findByCartCartId(testCart.getCartId());
        assert itemsInDb.size() >= 1 : "Should have at least 1 cart item in DB: " + itemsInDb.size();
        
        mockMvc.perform(get("/api/cart/account/{accountId}/items", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].productId").value(testProduct.getProductId()));
    }

    /**
     * Test Case 11: Xóa tất cả sản phẩm trong giỏ hàng
     * Mục đích: Kiểm tra API DELETE /api/cart/account/{accountId}/clear
     * Input: accountId của testAccount
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Tất cả cart items bị xóa khỏi database
     * - Cart vẫn còn nhưng rỗng (không có items)
     * Use case: Khách hàng muốn làm mới giỏ hàng
     */
    @Test
    @DisplayName("Integration Test 11: Clear cart - Thành công")
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/account/{accountId}/clear", testAccount.getAccountId()))
                .andExpect(status().isOk());

        // Verify trong database - cart items đã bị xóa
        long count = cartItemRepository.findByCartCartId(testCart.getCartId()).size();
        assert count == 0;
    }

    /**
     * Test Case 12: Vô hiệu hóa giỏ hàng
     * Mục đích: Kiểm tra API PUT /api/cart/{cartId}/disable
     * Input: cartId của testCart
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response status = 0 (disabled)
     * - Database: cart.status được set về 0
     * Business logic: Cart bị disable không thể thêm/sửa items
     */
    @Test
    @DisplayName("Integration Test 12: Disable cart - Thành công")
    void testDisableCart_Success() throws Exception {
        mockMvc.perform(put("/api/cart/{cartId}/disable", testCart.getCartId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));

        // Verify trong database
        Cart disabled = cartRepository.findById(testCart.getCartId()).orElseThrow();
        assert disabled.getStatus() == 0;
    }

    /**
     * Test Case 13: Kích hoạt lại giỏ hàng
     * Mục đích: Kiểm tra API PUT /api/cart/{cartId}/enable
     * Setup: Disable cart trước (status = 0)
     * Input: cartId của testCart
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response status = 1 (enabled)
     * - Database: cart.status được set về 1
     * Business logic: Cart được enable có thể hoạt động bình thường
     */
    @Test
    @DisplayName("Integration Test 13: Enable cart - Thành công")
    void testEnableCart_Success() throws Exception {
        // Disable trước
        testCart.setStatus(0);
        cartRepository.save(testCart);

        mockMvc.perform(put("/api/cart/{cartId}/enable", testCart.getCartId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));

        // Verify trong database
        Cart enabled = cartRepository.findById(testCart.getCartId()).orElseThrow();
        assert enabled.getStatus() == 1;
    }

    /**
     * Test Case 14: Cập nhật trạng thái giỏ hàng
     * Mục đích: Kiểm tra API PUT /api/cart/{cartId}/status/{status}
     * Input: cartId và status mới = 0
     * Kết quả mong muốn:
     * - HTTP Status: 200 OK
     * - Response status = 0
     * - Database: cart.status được cập nhật
     */
    @Test
    @DisplayName("Integration Test 14: Update cart status - Thành công")
    void testUpdateCartStatus_Success() throws Exception {
        mockMvc.perform(put("/api/cart/{cartId}/status/{status}", testCart.getCartId(), 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));

        // Verify trong database
        Cart updated = cartRepository.findById(testCart.getCartId()).orElseThrow();
        assert updated.getStatus() == 0;
    }

    /**
     * Test Case 15: Thêm sản phẩm không tồn tại vào giỏ
     * Mục đích: Kiểm tra xử lý lỗi khi thêm product ID không hợp lệ
     * Input: CreateCartItemRequest với productId = "invalid-product-id"
     * Kết quả mong muốn:
     * - HTTP Status: 400 Bad Request
     * - Không tạo cart item mới trong database
     * Business logic: Validate product tồn tại trước khi thêm vào cart
     */
    @Test
    @DisplayName("Integration Test 15: Thêm cart item - Product không tồn tại")
    void testAddCartItem_ProductNotFound() throws Exception {
        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId("invalid-product-id");
        request.setQuantity(1);

        mockMvc.perform(post("/api/cart/account/{accountId}/items", testAccount.getAccountId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test Case 16: Cập nhật cart item không tồn tại
     * Mục đích: Kiểm tra xử lý lỗi khi update với ID không hợp lệ
     * Input: UpdateCartItemRequest với cartItemId = "invalid-cart-item-id"
     * Kết quả mong muốn:
     * - HTTP Status: 400 Bad Request
     * - Không có thay đổi trong database
     * Business logic: Validate cart item tồn tại trước khi update
     */
    @Test
    @DisplayName("Integration Test 16: Update cart item - CartItem không tồn tại")
    void testUpdateCartItem_NotFound() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/cart/items/{cartItemId}", "invalid-cart-item-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
