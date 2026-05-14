package fruitshop.catalog_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.catalog_service.dto.request.Product.CreateProductImageRequest;
import fruitshop.catalog_service.dto.request.Product.CreateProductRequest;
import fruitshop.catalog_service.dto.request.Product.UpdateProductRequest;
import fruitshop.catalog_service.entity.Category;
import fruitshop.catalog_service.entity.Product;
import fruitshop.catalog_service.entity.ProductImage;
import fruitshop.catalog_service.repository.CategoryRepository;
import fruitshop.catalog_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test cho Product API
 * Product Status: 0=Ẩn, 1=Hiển thị
 */
class ProductIntegrationTest extends BaseIntegrationTest {

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
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        
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
        testProduct.setCreatedAt(Instant.now());
        testProduct.setUpdatedAt(Instant.now());

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
     * Mục đích: Kiểm tra API GET /api/catalog/products trả về danh sách products
     */
    @Test
    @DisplayName("Test 1: getAllProduct - Lấy tất cả product với phân trang")
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/catalog/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /**
     * Test 2: Lấy product theo ID
     * Mục đích: Kiểm tra API GET /api/catalog/products/{id} trả về thông tin chi tiết product
     */
    @Test
    @DisplayName("Test 2: getByProductId - Lấy product theo ID")
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/catalog/products/{productId}", testProduct.getProductId()))
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
     * Mục đích: Kiểm tra API POST /api/catalog/products tạo product mới vào database
     */
    @Test
    @DisplayName("Test 3: createProduct - Tạo product mới")
    void testCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Dừa Xiêm");
        request.setPrice(25000L);
        request.setStock(200L);
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

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Lưu ý: controller ở đây trả 200 OK thay vì 201 Created
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
     * Mục đích: Kiểm tra API PUT /api/catalog/products/{id} cập nhật thông tin product
     */
    @Test
    @DisplayName("Test 4: updateProduct - Cập nhật product")
    void testUpdateProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Xoài Cát Hòa Lộc Premium");
        request.setPrice(200000L);
        request.setStock(50L);
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

        mockMvc.perform(put("/api/catalog/products/{productId}", testProduct.getProductId())
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
     * Mục đích: Kiểm tra API DELETE /api/catalog/products/{id} xóa product khỏi database
     */
    @Test
    @DisplayName("Test 5: deleteProduct - Xóa product")
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/catalog/products/{productId}", testProduct.getProductId()))
                .andExpect(status().isNoContent());

        // Verify trong database
        boolean exists = productRepository.existsById(testProduct.getProductId());
        assert !exists;
    }
}
