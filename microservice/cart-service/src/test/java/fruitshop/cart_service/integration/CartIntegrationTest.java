package fruitshop.cart_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import fruitshop.cart_service.dto.request.CreateCartItemRequest;
import fruitshop.cart_service.dto.request.UpdateCartItemRequest;
import fruitshop.cart_service.entity.Cart;
import fruitshop.cart_service.entity.CartItem;
import fruitshop.cart_service.repository.CartItemRepository;
import fruitshop.cart_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
@WithMockUser(authorities = "ROLE_ADMIN")
class CartIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Cart testCart;
    private CartItem testCartItem;
    
    private final String TEST_ACCOUNT_ID = UUID.randomUUID().toString();
    private final String TEST_PRODUCT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        wireMock.resetAll();

        // Mock AccountClient
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/account/" + TEST_ACCOUNT_ID))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "accountId": "%s",
                              "accountName": "Nguyễn Văn A",
                              "accountPhone": "0355142890",
                              "status": 1
                            }
                            """.formatted(TEST_ACCOUNT_ID))));

        // Mock ProductClient cho sản phẩm ban đầu
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/catalog/products/" + TEST_PRODUCT_ID))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "productId": "%s",
                              "productName": "Xoài Úc",
                              "price": 50000,
                              "status": 1,
                              "images": []
                            }
                            """.formatted(TEST_PRODUCT_ID))));

        // Tạo cart
        testCart = new Cart();
        testCart.setAccountId(TEST_ACCOUNT_ID);
        testCart.setStatus(1);
        testCart.setCreatedAt(Instant.now());
        testCart = cartRepository.save(testCart);

        // Tạo cart item
        testCartItem = new CartItem();
        testCartItem.setCart(testCart);
        testCartItem.setProductId(TEST_PRODUCT_ID);
        testCartItem.setQuantity(2);
        testCartItem = cartItemRepository.save(testCartItem);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả giỏ hàng với phân trang
     * Mục đích: Kiểm tra API GET /api/cart trả về danh sách carts
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
     * Mục đích: Kiểm tra API GET /api/cart/id/{id} trả về đúng thông tin cart
     */
    @Test
    @DisplayName("Integration Test 2: Lấy cart theo ID - Thành công")
    void testGetCartById_Success() throws Exception {
        mockMvc.perform(get("/api/cart/id/{id}", testCart.getCartId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
                .andExpect(jsonPath("$.status").value(1));
    }

    /**
     * Test Case 4: Lấy giỏ hàng theo tài khoản
     * Mục đích: Kiểm tra API GET /api/cart/account/{accountId}
     */
    @Test
    @DisplayName("Integration Test 4: Lấy cart theo accountId - Thành công")
    void testGetCartByAccountId_Success() throws Exception {
        mockMvc.perform(get("/api/cart/account/{accountId}", TEST_ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.accountId").value(TEST_ACCOUNT_ID));
    }

    /**
     * Test Case 7: Thêm sản phẩm vào giỏ hàng
     * Mục đích: Kiểm tra API POST /api/cart/account/{accountId}/items
     */
    @Test
    @DisplayName("Integration Test 7: Thêm cart item - Thành công")
    void testAddCartItem_Success() throws Exception {
        String newProductId = UUID.randomUUID().toString();
        
        // Mock cho product mới
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/catalog/products/" + newProductId))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "productId": "%s",
                              "productName": "Táo Fuji",
                              "price": 60000,
                              "status": 1,
                              "images": []
                            }
                            """.formatted(newProductId))));

        CreateCartItemRequest request = new CreateCartItemRequest();
        request.setProductId(newProductId);
        request.setQuantity(3);

        mockMvc.perform(post("/api/cart/account/{accountId}/items", TEST_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // Assert cart response chứa tổng quan về giỏ hàng (không trả trực tiếp cartItem)
                .andExpect(jsonPath("$.items", hasSize(2))) // Có 2 items (1 cũ, 1 mới)
                .andExpect(jsonPath("$.items[1].productId").value(newProductId))
                .andExpect(jsonPath("$.items[1].quantity").value(3))
                .andExpect(jsonPath("$.items[1].productName").value("Táo Fuji"))
                .andExpect(jsonPath("$.items[1].productPrice").value(60000));

        // Verify trong database
        long count = cartItemRepository.count();
        assert count == 2;
    }

    /**
     * Test Case 8: Cập nhật số lượng sản phẩm trong giỏ
     * Mục đích: Kiểm tra API PUT /api/cart/items/{cartItemId}
     */
    @Test
    @DisplayName("Integration Test 8: Cập nhật cart item - Thành công")
    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/cart/items/{cartItemId}", testCartItem.getCartItemId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify trong database
        CartItem updated = cartItemRepository.findById(testCartItem.getCartItemId()).orElseThrow();
        assert updated.getQuantity() == 5;
    }

    /**
     * Test Case 9: Xóa sản phẩm khỏi giỏ hàng
     * Mục đích: Kiểm tra API DELETE /api/cart/items/{cartItemId}
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
     * Test Case 11: Xóa tất cả sản phẩm trong giỏ hàng
     * Mục đích: Kiểm tra API DELETE /api/cart/account/{accountId}/clear
     */
    @Test
    @DisplayName("Integration Test 11: Clear cart - Thành công")
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/cart/account/{accountId}/clear", TEST_ACCOUNT_ID))
                .andExpect(status().isOk());

        // Verify trong database - cart items đã bị xóa
        List<CartItem> items = cartItemRepository.findByCartCartId(testCart.getCartId());
        assert items.isEmpty();
    }
}
