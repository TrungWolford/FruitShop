package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Product.CreateProductRequest;
import server.FruitShop.dto.request.Product.UpdateProductRequest;
import server.FruitShop.dto.response.Product.ProductResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.entity.Product;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.repository.ProductImageRepository;
import server.FruitShop.repository.ProductRepository;
import server.FruitShop.service.Impl.ProductServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho ProductService
 * Class này test tất cả các chức năng liên quan đến quản lý sản phẩm
 * bao gồm: tạo, xem, cập nhật, xóa, tìm kiếm sản phẩm và quản lý categories
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Product Service")
class ProductServiceImplTest {

    @Mock // Mock repository quản lý sản phẩm
    private ProductRepository productRepository;

    @Mock // Mock repository quản lý danh mục
    private CategoryRepository categoryRepository;

    @Mock // Mock repository quản lý hình ảnh sản phẩm
    private ProductImageRepository productImageRepository;

    @InjectMocks // Inject tất cả mock vào service
    private ProductServiceImpl productService;

    // Các entity mẫu để test
    private Product testProduct;
    private Category testCategory;
    private Pageable pageable;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo product và category mẫu với đầy đủ thông tin
     */
    @BeforeEach
    void setUp() {
        // Tạo category test
        testCategory = new Category();
        testCategory.setCategoryId("cat-001");
        testCategory.setCategoryName("Trái cây");

        // Tạo product test với thông tin chi tiết
        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài Úc");
        testProduct.setPrice(50000); // 50,000 VND/kg
        testProduct.setStock(100); // 100 kg trong kho
        testProduct.setDescription("Xoài nhập khẩu Úc");
        testProduct.setStatus(1); // 1 = Đang bán
        testProduct.setCategories(new ArrayList<>(List.of(testCategory)));  // Mutable list cho testing
        testProduct.setImages(new ArrayList<>()); // Danh sách hình ảnh rỗng

        // Pageable cho phân trang
        pageable = PageRequest.of(0, 10);
    }

    /**
     * Test case 1: Kiểm tra lấy product theo ID
     * Kịch bản: Tìm product với ID tồn tại, bao gồm categories và images
     * Kết quả mong đợi: Trả về ProductResponse với đầy đủ thông tin
     */
    @Test
    @DisplayName("Test 1: Lấy product theo ID - Thành công")
    void testGetByProductId_Success() {
        // ARRANGE - Mock repository trả về product với categories và images
        when(productRepository.findByIdWithCategories("prod-001")).thenReturn(testProduct);
        when(productRepository.findByIdWithImages("prod-001")).thenReturn(testProduct);

        // ACT - Gọi service để lấy product
        ProductResponse result = productService.getByProductId("prod-001");

        // ASSERT - Verify thông tin product đầy đủ
        assertNotNull(result);
        assertEquals("prod-001", result.getProductId()); // ID đúng
        assertEquals("Xoài Úc", result.getProductName()); // Tên đúng
        assertEquals(50000, result.getPrice()); // Giá đúng
        verify(productRepository, times(1)).findByIdWithCategories("prod-001"); // Load categories
        verify(productRepository, times(1)).findByIdWithImages("prod-001"); // Load images
    }

    /**
     * Test case 2: Kiểm tra lấy product với ID không tồn tại
     * Kịch bản: Tìm product với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException với message "Product not found"
     */
    @Test
    @DisplayName("Test 2: Lấy product theo ID - Không tìm thấy")
    void testGetByProductId_NotFound() {
        // ARRANGE - Mock repository trả về null khi không tìm thấy
        when(productRepository.findByIdWithCategories("invalid-id")).thenReturn(null);

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getByProductId("invalid-id");
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, times(1)).findByIdWithCategories("invalid-id");
    }

    /**
     * Test case 3: Kiểm tra tạo product mới
     * Kịch bản: Tạo product với thông tin hợp lệ và gán categories
     * Kết quả mong đợi: Product được tạo và lưu với categories
     */
    @Test
    @DisplayName("Test 3: Tạo product mới - Thành công")
    void testCreateProduct_Success() {
        // ARRANGE - Tạo request với thông tin product
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Cam Sành");
        request.setPrice(30000); // 30,000 VND/kg
        request.setStock(200); // 200 kg
        request.setDescription("Cam tươi Việt Nam");
        request.setCategoryIds(List.of("cat-001")); // Gán vào category trái cây

        // Mock repository lưu product và tìm categories
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory));

        // ACT - Gọi service để tạo product
        ProductResponse result = productService.createProduct(request);

        // ASSERT - Verify product được tạo
        assertNotNull(result);
        verify(productRepository, times(2)).save(any(Product.class)); // 2 lần: lần đầu và sau khi set categories
        verify(categoryRepository, times(1)).findAllById(anyList()); // Tìm categories
    }

    /**
     * Test case 4: Kiểm tra tạo product với categories không tồn tại
     * Kịch bản: Request có 2 category IDs nhưng chỉ tìm thấy 1 category
     * Kết quả mong đợi: Throw RuntimeException, không tạo product
     */
    @Test
    @DisplayName("Test 4: Tạo product - Categories không tồn tại")
    void testCreateProduct_CategoriesNotFound() {
        // ARRANGE - Request có 2 categories nhưng chỉ tìm thấy 1
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Test Product");
        request.setPrice(10000);
        request.setStock(50);
        request.setCategoryIds(List.of("cat-001", "cat-002")); // 2 categories

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory)); // Chỉ tìm thấy 1

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(request);
        });

        // Verify message về categories không tồn tại
        assertTrue(exception.getMessage().contains("Some categories were not found"));
    }

    /**
     * Test case 5: Kiểm tra cập nhật product
     * Kịch bản: Cập nhật thông tin product như tên, giá, stock, categories
     * Kết quả mong đợi: Product được cập nhật với thông tin mới
     */
    @Test
    @DisplayName("Test 5: Cập nhật product - Thành công")
    void testUpdateProduct_Success() {
        // ARRANGE - Tạo request với thông tin cập nhật
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Xoài Úc Premium"); // Tên mới
        request.setPrice(60000); // Tăng giá lên 60k
        request.setStock(80); // Giảm stock xuống 80kg
        request.setDescription("Xoài cao cấp");
        request.setStatus(1); // Vẫn đang bán
        request.setCategoryIds(List.of("cat-001"));

        // Mock repository tìm product và categories, sau đó lưu
        when(productRepository.findByIdWithCategories("prod-001")).thenReturn(testProduct);
        when(productRepository.findByIdWithImages("prod-001")).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(testProduct);

        // ACT - Cập nhật product
        ProductResponse result = productService.updateProduct(request, "prod-001");

        // ASSERT - Verify product đã được cập nhật
        assertNotNull(result);
        verify(productRepository, times(1)).findByIdWithCategories("prod-001");
        verify(productRepository, times(1)).saveAndFlush(any(Product.class));
    }

    /**
     * Test case 6: Kiểm tra cập nhật product không tồn tại
     * Kịch bản: Cố gắng cập nhật product với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException, không thực hiện cập nhật
     */
    @Test
    @DisplayName("Test 6: Cập nhật product - Không tìm thấy")
    void testUpdateProduct_NotFound() {
        // ARRANGE - Request cập nhật
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Test");
        request.setPrice(10000);
        request.setStock(10);
        request.setStatus(1);

        // Mock repository không tìm thấy product
        when(productRepository.findByIdWithCategories("invalid-id")).thenReturn(null);

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(request, "invalid-id");
        });

        // Verify exception message
        assertTrue(exception.getMessage().contains("Product not found") || 
                   exception.getMessage().contains("Failed to update product"));
        verify(productRepository, times(1)).findByIdWithCategories("invalid-id");
    }

    /**
     * Test case 7: Kiểm tra xóa product
     * Kịch bản: Xóa product cùng với tất cả images liên quan
     * Kết quả mong đợi: Product và images được xóa khỏi database
     */
    @Test
    @DisplayName("Test 7: Xóa product - Thành công")
    void testDeleteProduct_Success() {
        // ARRANGE - Mock repository tìm product và xóa
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        doNothing().when(productImageRepository).deleteByProductProductId("prod-001"); // Xóa images trước
        doNothing().when(productRepository).delete(testProduct); // Xóa product

        // ACT - Gọi service để xóa product
        productService.deleteProduct("prod-001");

        // ASSERT - Verify cả images và product đều được xóa
        verify(productRepository, times(1)).findById("prod-001");
        verify(productImageRepository, times(1)).deleteByProductProductId("prod-001"); // Xóa images trước
        verify(productRepository, times(1)).delete(testProduct); // Sau đó xóa product
    }

    /**
     * Test case 8: Kiểm tra xóa product không tồn tại
     * Kịch bản: Cố gắng xóa product với ID không có trong database
     * Kết quả mong đợi: Throw RuntimeException, không thực hiện xóa
     */
    @Test
    @DisplayName("Test 8: Xóa product - Không tìm thấy")
    void testDeleteProduct_NotFound() {
        // ARRANGE - Mock repository không tìm thấy product
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT - Verify exception được throw
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct("invalid-id");
        });

        // Verify exception message và không có xóa nào được thực hiện
        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, times(1)).findById("invalid-id");
        verify(productRepository, never()).delete(any(Product.class)); // Không xóa
    }

    /**
     * Test case 9: Kiểm tra tìm kiếm product theo tên
     * Kịch bản: Tìm kiếm product với keyword "Xoài"
     * Kết quả mong đợi: Trả về Page chứa các products có tên chứa keyword
     */
    @Test
    @DisplayName("Test 9: Tìm kiếm product - Thành công")
    void testSearchProduct_Success() {
        // ARRANGE - Chuẩn bị keyword và kết quả tìm kiếm
        String keyword = "Xoài";
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        // Mock repository trả về products chứa keyword
        when(productRepository.findByProductName(keyword, pageable)).thenReturn(productPage);

        // ACT - Tìm kiếm product (không filter theo giá)
        Page<ProductResponse> result = productService.searchProduct(keyword, null, null, pageable);

        // ASSERT - Verify kết quả tìm kiếm
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // Tìm thấy 1 product
        assertEquals("Xoài Úc", result.getContent().get(0).getProductName()); // Tên product đúng
        verify(productRepository, times(1)).findByProductName(keyword, pageable);
    }

    /**
     * Test case 10: Kiểm tra lấy top products bán chạy
     * Kịch bản: Lấy top 10 products có stock thấp nhất (bán chạy nhất)
     * Kết quả mong đợi: Trả về danh sách products sắp xếp theo stock tăng dần
     */
    @Test
    @DisplayName("Test 10: Lấy top sold products - Thành công")
    void testGetTopSoldProduct_Success() {
        // ARRANGE - Mock repository trả về top products
        List<Product> topProducts = List.of(testProduct);
        when(productRepository.findTop10ByOrderByStockAsc()).thenReturn(topProducts);

        // ACT - Lấy top products bán chạy
        List<ProductResponse> result = productService.getTopSoldProduct();

        // ASSERT - Verify danh sách top products
        assertNotNull(result);
        assertEquals(1, result.size()); // 1 product trong top
        assertEquals("Xoài Úc", result.get(0).getProductName());
        verify(productRepository, times(1)).findTop10ByOrderByStockAsc(); // Sắp xếp theo stock tăng dần
    }

    /**
     * Test case 11: Kiểm tra lấy tất cả products với phân trang
     * Kịch bản: Lấy danh sách tất cả products, load thêm categories cho mỗi product
     * Kết quả mong đợi: Trả về Page chứa products với đầy đủ categories
     */
    @Test
    @DisplayName("Test 11: Lấy tất cả products - Thành công")
    void testGetAllProduct_Success() {
        // ARRANGE - Tạo page chứa products
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        // Mock repository trả về page và load categories
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productRepository.findByIdsWithCategories(anyList())).thenReturn(products);

        // ACT - Lấy tất cả products
        Page<ProductResponse> result = productService.getAllProduct(pageable);

        // ASSERT - Verify page result
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable); // Lấy page
        verify(productRepository, times(1)).findByIdsWithCategories(anyList()); // Load categories cho products
    }

    /**
     * Test case 12: Kiểm tra tìm kiếm product với khoảng giá
     * Kịch bản: Tìm kiếm "Xoài" với giá từ 40k đến 60k
     * Kết quả mong đợi: Trả về products thỏa mãn keyword và khoảng giá
     */
    @Test
    @DisplayName("Test 12: Tìm kiếm product với price range - Thành công")
    void testSearchProduct_WithPriceRange() {
        // ARRANGE - Chuẩn bị keyword và khoảng giá
        String keyword = "Xoài";
        Double minPrice = 40000.0; // Giá tối thiểu 40k
        Double maxPrice = 60000.0; // Giá tối đa 60k
        
        List<Product> products = List.of(testProduct); // testProduct có giá 50k, nằm trong khoảng
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        // Mock repository tìm kiếm theo tên
        when(productRepository.findByProductName(keyword, pageable)).thenReturn(productPage);

        // ACT - Tìm kiếm với price filter
        Page<ProductResponse> result = productService.searchProduct(keyword, minPrice, maxPrice, pageable);

        // ASSERT - Verify kết quả filter theo giá
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // Product có giá 50k thỏa mãn điều kiện
        verify(productRepository, times(1)).findByProductName(keyword, pageable);
    }
}
