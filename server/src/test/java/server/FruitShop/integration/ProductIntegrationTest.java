package server.FruitShop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Product.CreateProductImageRequest;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.entity.Category;
import server.FruitShop.entity.Product;
import server.FruitShop.entity.ProductImage;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Product API
 * Product Status: 0=Ẩn, 1=Hiển thị
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@Transactional // Rollback sau mỗi test
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // @Transactional sẽ tự động rollback sau mỗi test
        
        // Tạo category test
        testCategory = new Category();
        testCategory.setCategoryName("Trái cây nhiệt đới");
        testCategory.setStatus(1);
        testCategory = categoryRepository.save(testCategory);

        // Tạo product test
        testProduct = new Product();
        testProduct.setProductName("Xoài Cát Hòa Lộc");
        testProduct.setPrice(150000);
        testProduct.setStock(100);
        testProduct.setDescription("Xoài Cát Hòa Lộc Tiền Giang - Ngọt thơm đặc biệt");
        testProduct.setStatus(1);
        testProduct.setCreatedAt(new Date());
        testProduct.setUpdatedAt(new Date());

        // Set categories
        List<Category> categories = new ArrayList<>();
        categories.add(testCategory);
        testProduct.setCategories(categories);

        // Set images
        List<ProductImage> images = new ArrayList<>();
        ProductImage mainImage = new ProductImage();
        mainImage.setImageUrl("https://example.com/xoai-main.jpg");
        mainImage.setImageOrder(1);
        mainImage.setIsMain(true);
        mainImage.setProduct(testProduct);
        images.add(mainImage);

        ProductImage secondImage = new ProductImage();
        secondImage.setImageUrl("https://example.com/xoai-2.jpg");
        secondImage.setImageOrder(2);
        secondImage.setIsMain(false);
        secondImage.setProduct(testProduct);
        images.add(secondImage);

        testProduct.setImages(images);
        testProduct = productRepository.save(testProduct);
    }

    /**
     * Test 1: Lấy tất cả products với phân trang
     * Mục đích: Kiểm tra API GET /api/product trả về danh sách products
     * Input: page=0, size=10
     */
    @Test
    @DisplayName("Test 1: getAllProduct - Lấy tất cả product với phân trang")
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/product")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
                // Không kiểm tra tên cụ thể vì có thể có nhiều products từ tests khác
    }

    /**
     * Test 2: Lấy product theo ID
     * Mục đích: Kiểm tra API GET /api/product/{id} trả về thông tin chi tiết product
     * Input: productId hợp lệ
     */
    @Test
    @DisplayName("Test 2: getByProductId - Lấy product theo ID")
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/product/{productId}", testProduct.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Xoài Cát Hòa Lộc"))
                .andExpect(jsonPath("$.price").value(150000))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Trái cây nhiệt đới"))
                .andExpect(jsonPath("$.images", hasSize(2)));
    }

    /**
     * Test 3: Tạo product mới
     * Mục đích: Kiểm tra API POST /api/product tạo product mới vào database
     * Input: CreateProductRequest (name, price, stock, description, categoryIds, images)
     */
    @Test
    @DisplayName("Test 3: createProduct - Tạo product mới")
    void testCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Dừa Xiêm");
        request.setPrice(25000);
        request.setStock(200);
        request.setDescription("Dừa Xiêm Bến Tre - Nước ngọt tự nhiên");

        // Set categories
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add(testCategory.getCategoryId());
        request.setCategoryIds(categoryIds);

        // Set images
        List<CreateProductImageRequest> imageRequests = new ArrayList<>();
        CreateProductImageRequest mainImageReq = new CreateProductImageRequest();
        mainImageReq.setImageUrl("https://example.com/dua-main.jpg");
        mainImageReq.setImageOrder(1);
        mainImageReq.setIsMain(true);
        imageRequests.add(mainImageReq);
        request.setImages(imageRequests);

        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Dừa Xiêm"))
                .andExpect(jsonPath("$.price").value(25000))
                .andExpect(jsonPath("$.stock").value(200))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.images", hasSize(1)));

        // Verify trong database
        long count = productRepository.count();
        assert count >= 2; // Ít nhất 2 products
    }

    /**
     * Test 4: Cập nhật product
     * Mục đích: Kiểm tra API PUT /api/product/{id} cập nhật thông tin product
     * Input: UpdateProductRequest (name, price, stock, description, status, categoryIds, images)
     */
    @Test
    @DisplayName("Test 4: updateProduct - Cập nhật product")
    void testUpdateProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Xoài Cát Hòa Lộc Premium");
        request.setPrice(200000);
        request.setStock(50);
        request.setDescription("Xoài Cát Hòa Lộc cao cấp");
        request.setStatus(1);

        // Set categories
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add(testCategory.getCategoryId());
        request.setCategoryIds(categoryIds);

        // Set images
        List<CreateProductImageRequest> imageRequests = new ArrayList<>();
        CreateProductImageRequest mainImageReq = new CreateProductImageRequest();
        mainImageReq.setImageUrl("https://example.com/xoai-premium.jpg");
        mainImageReq.setImageOrder(1);
        mainImageReq.setIsMain(true);
        imageRequests.add(mainImageReq);
        request.setImages(imageRequests);

        mockMvc.perform(put("/api/product/{productId}", testProduct.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Xoài Cát Hòa Lộc Premium"))
                .andExpect(jsonPath("$.price").value(200000))
                .andExpect(jsonPath("$.stock").value(50));

        // Verify trong database
        Product updated = productRepository.findById(testProduct.getProductId()).orElseThrow();
        assert updated.getProductName().equals("Xoài Cát Hòa Lộc Premium");
        assert updated.getPrice() == 200000;
    }

    /**
     * Test 5: Xóa product
     * Mục đích: Kiểm tra API DELETE /api/product/{id} xóa product khỏi database
     * Input: productId hợp lệ
     */
    @Test
    @DisplayName("Test 5: deleteProduct - Xóa product")
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/product/{productId}", testProduct.getProductId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = productRepository.existsById(testProduct.getProductId());
        assert !exists;
    }

    /**
     * Test 6: Lọc products theo category và khoảng giá
     * Mục đích: Kiểm tra API GET /api/product/filter lọc products theo nhiều tiêu chí
     * Input: categoryId, status=1, minPrice=100000, maxPrice=200000, page=0, size=10
     */
    @Test
    @DisplayName("Test 6: filterProduct - Filter products theo category và giá")
    void testFilterProduct() throws Exception {
        mockMvc.perform(get("/api/product/filter")
                        .param("categoryId", testCategory.getCategoryId())
                        .param("status", "1")
                        .param("minPrice", "100000")
                        .param("maxPrice", "200000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].productName").value("Xoài Cát Hòa Lộc"))
                .andExpect(jsonPath("$.content[0].price", allOf(
                        greaterThanOrEqualTo(100000),
                        lessThanOrEqualTo(200000)
                )));
    }

    /**
     * Test 7: Tìm kiếm products theo từ khóa
     * Mục đích: Kiểm tra API GET /api/product/search tìm products theo keywords và giá
     * Input: keywords="Xoài", minPrice=100000, maxPrice=200000, page=0, size=10
     */
    @Test
    @DisplayName("Test 7: searchProduct - Search products theo từ khóa")
    void testSearchProduct() throws Exception {
        mockMvc.perform(get("/api/product/search")
                        .param("keywords", "Xoài")
                        .param("minPrice", "100000")
                        .param("maxPrice", "200000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productName", containsString("Xoài")))
                .andExpect(jsonPath("$.content[0].price").value(150000));
    }

    /**
     * Test 8: Lấy top 10 products bán chạy
     * Mục đích: Kiểm tra API GET /api/product/top-10 trả về products có stock thấp (bán chạy)
     * Input: Không có (lấy 10 products có stock thấp nhất)
     */
    @Test
    @DisplayName("Test 8: getTopSoldProduct - Lấy top 10 products bán chạy")
    void testGetTopSoldProduct() throws Exception {
        // Tạo thêm products với stock khác nhau
        Product product2 = new Product();
        product2.setProductName("Sầu riêng Ri6");
        product2.setPrice(300000);
        product2.setStock(5); // Stock thấp hơn
        product2.setStatus(1);
        product2.setCreatedAt(new Date());
        product2.setUpdatedAt(new Date());
        product2.setCategories(List.of(testCategory));
        productRepository.save(product2);

        mockMvc.perform(get("/api/product/top-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].stock", lessThanOrEqualTo(100)));
    }

    /**
     * Test 9: Dọn dẹp duplicate images
     * Mục đích: Kiểm tra API POST /api/product/{id}/cleanup-images xóa ảnh trùng lặp
     * Input: productId hợp lệ
     */
    @Test
    @DisplayName("Test 9: cleanupDuplicateImages - Cleanup duplicate images")
    void testCleanupDuplicateImages() throws Exception {
        mockMvc.perform(post("/api/product/{productId}/cleanup-images", testProduct.getProductId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Duplicate images cleaned up successfully"));
    }
}
