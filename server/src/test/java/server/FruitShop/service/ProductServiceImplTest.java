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
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Product Service")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setCategoryId("cat-001");
        testCategory.setCategoryName("Trái cây");

        testProduct = new Product();
        testProduct.setProductId("prod-001");
        testProduct.setProductName("Xoài Úc");
        testProduct.setPrice(50000);
        testProduct.setStock(100);
        testProduct.setDescription("Xoài nhập khẩu Úc");
        testProduct.setStatus(1);
        testProduct.setCategories(new ArrayList<>(List.of(testCategory)));  // Mutable list
        testProduct.setImages(new ArrayList<>());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Test 1: Lấy product theo ID - Thành công")
    void testGetByProductId_Success() {
        // ARRANGE
        when(productRepository.findByIdWithCategories("prod-001")).thenReturn(testProduct);
        when(productRepository.findByIdWithImages("prod-001")).thenReturn(testProduct);

        // ACT
        ProductResponse result = productService.getByProductId("prod-001");

        // ASSERT
        assertNotNull(result);
        assertEquals("prod-001", result.getProductId());
        assertEquals("Xoài Úc", result.getProductName());
        assertEquals(50000, result.getPrice());
        verify(productRepository, times(1)).findByIdWithCategories("prod-001");
        verify(productRepository, times(1)).findByIdWithImages("prod-001");
    }

    @Test
    @DisplayName("Test 2: Lấy product theo ID - Không tìm thấy")
    void testGetByProductId_NotFound() {
        // ARRANGE
        when(productRepository.findByIdWithCategories("invalid-id")).thenReturn(null);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getByProductId("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, times(1)).findByIdWithCategories("invalid-id");
    }

    @Test
    @DisplayName("Test 3: Tạo product mới - Thành công")
    void testCreateProduct_Success() {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Cam Sành");
        request.setPrice(30000);
        request.setStock(200);
        request.setDescription("Cam tươi Việt Nam");
        request.setCategoryIds(List.of("cat-001"));

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory));

        // ACT
        ProductResponse result = productService.createProduct(request);

        // ASSERT
        assertNotNull(result);
        verify(productRepository, times(2)).save(any(Product.class)); // 2 lần: lần đầu và sau khi set categories
        verify(categoryRepository, times(1)).findAllById(anyList());
    }

    @Test
    @DisplayName("Test 4: Tạo product - Categories không tồn tại")
    void testCreateProduct_CategoriesNotFound() {
        // ARRANGE
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Test Product");
        request.setPrice(10000);
        request.setStock(50);
        request.setCategoryIds(List.of("cat-001", "cat-002")); // 2 categories

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory)); // Chỉ tìm thấy 1

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(request);
        });

        assertTrue(exception.getMessage().contains("Some categories were not found"));
    }

    @Test
    @DisplayName("Test 5: Cập nhật product - Thành công")
    void testUpdateProduct_Success() {
        // ARRANGE
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Xoài Úc Premium");
        request.setPrice(60000);
        request.setStock(80);
        request.setDescription("Xoài cao cấp");
        request.setStatus(1);
        request.setCategoryIds(List.of("cat-001"));

        when(productRepository.findByIdWithCategories("prod-001")).thenReturn(testProduct);
        when(productRepository.findByIdWithImages("prod-001")).thenReturn(testProduct);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(testCategory));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(testProduct);

        // ACT
        ProductResponse result = productService.updateProduct(request, "prod-001");

        // ASSERT
        assertNotNull(result);
        verify(productRepository, times(1)).findByIdWithCategories("prod-001");
        verify(productRepository, times(1)).saveAndFlush(any(Product.class));
    }

    @Test
    @DisplayName("Test 6: Cập nhật product - Không tìm thấy")
    void testUpdateProduct_NotFound() {
        // ARRANGE
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Test");
        request.setPrice(10000);
        request.setStock(10);
        request.setStatus(1);

        when(productRepository.findByIdWithCategories("invalid-id")).thenReturn(null);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(request, "invalid-id");
        });

        assertTrue(exception.getMessage().contains("Product not found") || 
                   exception.getMessage().contains("Failed to update product"));
        verify(productRepository, times(1)).findByIdWithCategories("invalid-id");
    }

    @Test
    @DisplayName("Test 7: Xóa product - Thành công")
    void testDeleteProduct_Success() {
        // ARRANGE
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(testProduct));
        doNothing().when(productImageRepository).deleteByProductProductId("prod-001");
        doNothing().when(productRepository).delete(testProduct);

        // ACT
        productService.deleteProduct("prod-001");

        // ASSERT
        verify(productRepository, times(1)).findById("prod-001");
        verify(productImageRepository, times(1)).deleteByProductProductId("prod-001");
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    @DisplayName("Test 8: Xóa product - Không tìm thấy")
    void testDeleteProduct_NotFound() {
        // ARRANGE
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, times(1)).findById("invalid-id");
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("Test 9: Tìm kiếm product - Thành công")
    void testSearchProduct_Success() {
        // ARRANGE
        String keyword = "Xoài";
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByProductName(keyword, pageable)).thenReturn(productPage);

        // ACT
        Page<ProductResponse> result = productService.searchProduct(keyword, null, null, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Xoài Úc", result.getContent().get(0).getProductName());
        verify(productRepository, times(1)).findByProductName(keyword, pageable);
    }

    @Test
    @DisplayName("Test 10: Lấy top sold products - Thành công")
    void testGetTopSoldProduct_Success() {
        // ARRANGE
        List<Product> topProducts = List.of(testProduct);
        when(productRepository.findTop10ByOrderByStockAsc()).thenReturn(topProducts);

        // ACT
        List<ProductResponse> result = productService.getTopSoldProduct();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Xoài Úc", result.get(0).getProductName());
        verify(productRepository, times(1)).findTop10ByOrderByStockAsc();
    }

    @Test
    @DisplayName("Test 11: Lấy tất cả products - Thành công")
    void testGetAllProduct_Success() {
        // ARRANGE
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productRepository.findByIdsWithCategories(anyList())).thenReturn(products);

        // ACT
        Page<ProductResponse> result = productService.getAllProduct(pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
        verify(productRepository, times(1)).findByIdsWithCategories(anyList());
    }

    @Test
    @DisplayName("Test 12: Tìm kiếm product với price range - Thành công")
    void testSearchProduct_WithPriceRange() {
        // ARRANGE
        String keyword = "Xoài";
        Double minPrice = 40000.0;
        Double maxPrice = 60000.0;
        
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByProductName(keyword, pageable)).thenReturn(productPage);

        // ACT
        Page<ProductResponse> result = productService.searchProduct(keyword, minPrice, maxPrice, pageable);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findByProductName(keyword, pageable);
    }
}
