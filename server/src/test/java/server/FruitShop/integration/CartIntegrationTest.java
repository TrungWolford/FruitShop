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

        // Xóa dữ liệu cũ
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        accountRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        roleRepository.deleteAll();

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

    @Test
    @DisplayName("Integration Test 2: Lấy cart theo ID - Thành công")
    void testGetCartById_Success() throws Exception {
        mockMvc.perform(get("/api/cart/{id}", testCart.getCartId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    @DisplayName("Integration Test 3: Lấy cart theo ID - Không tồn tại")
    void testGetCartById_NotFound() throws Exception {
        mockMvc.perform(get("/api/cart/{id}", "invalid-cart-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test 4: Lấy cart theo accountId - Thành công")
    void testGetCartByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/cart/account/{accountId}", testAccount.getAccountId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId").value(testAccount.getAccountId()));
    }

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

    @Test
    @DisplayName("Integration Test 9: Xóa cart item - Thành công")
    void testRemoveCartItem_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", testCartItem.getCartItemId()))
                .andExpect(status().isOk());

        // Verify trong database
        boolean exists = cartItemRepository.existsById(testCartItem.getCartItemId());
        assert !exists;
    }

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

    @Test
    @DisplayName("Integration Test 11: Clear cart - Thành công")
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/account/{accountId}/clear", testAccount.getAccountId()))
                .andExpect(status().isOk());

        // Verify trong database - cart items đã bị xóa
        long count = cartItemRepository.findByCartCartId(testCart.getCartId()).size();
        assert count == 0;
    }

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
