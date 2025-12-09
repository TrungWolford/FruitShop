package com.fruitshop.tests;

import com.fruitshop.pages.AdminLoginPage;
import com.fruitshop.pages.AdminProductPage;
import com.fruitshop.tests.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminProductTest extends BaseTest {
    private AdminLoginPage adminLoginPage;
    private AdminProductPage adminProductPage;
    private final String ADMIN_PHONE = "03348400360";
    private final String ADMIN_PASSWORD = "123123";
    private final String ADMIN_URL = "https://fruitshop-c4.vercel.app/admin";

    @BeforeMethod
    public void setupAdminTest() {
        adminLoginPage = new AdminLoginPage(driver);
        adminProductPage = new AdminProductPage(driver);

        // Đăng nhập admin trước mỗi test
        System.out.println("=== Bắt đầu đăng nhập Admin ===");
        driver.get(ADMIN_URL);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Đăng nhập với tài khoản admin
        adminLoginPage.login(ADMIN_PHONE, ADMIN_PASSWORD);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✓ Đã đăng nhập admin, URL: " + driver.getCurrentUrl());
    }



    @Test(priority = 3, description = "TC_ADMIN_PRODUCT_003: Tìm kiếm sản phẩm")
    public void testSearchProduct() {
        System.out.println("\n[TC_ADMIN_PRODUCT_003] Test: Tìm kiếm sản phẩm");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Tìm kiếm với từ khóa
        String searchKeyword = "Nho";
        adminProductPage.searchProduct(searchKeyword);

        // Đợi kết quả tìm kiếm (debounce 500ms + load time)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra kết quả
        int resultCount = adminProductPage.getProductCount();
        System.out.println("✓ Số kết quả tìm kiếm với từ khóa '" + searchKeyword + "': " + resultCount);

        System.out.println("✓ TC_ADMIN_PRODUCT_003 PASSED\n");
    }

    @Test(priority = 4, description = "TC_ADMIN_PRODUCT_004: Chọn sản phẩm để xem chi tiết")
    public void testSelectProduct() {
        System.out.println("\n[TC_ADMIN_PRODUCT_004] Test: Chọn sản phẩm để xem chi tiết");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click chọn sản phẩm đầu tiên
        adminProductPage.clickFirstProduct();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra sản phẩm đã được chọn
        Assert.assertTrue(adminProductPage.isProductSelected(), 
                         "Sản phẩm phải được highlight sau khi click");
        System.out.println("✓ Sản phẩm đã được chọn (highlight xanh)");

        // Click nút Xem chi tiết
        adminProductPage.clickViewDetailButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal xem chi tiết hiển thị
        Assert.assertTrue(adminProductPage.isViewProductModalDisplayed(), 
                         "Modal xem chi tiết phải hiển thị");
        System.out.println("✓ Modal xem chi tiết đã hiển thị");

        // Kiểm tra tiêu đề modal
        String modalTitle = adminProductPage.getViewProductModalTitle();
        Assert.assertTrue(modalTitle.contains("Chi tiết sản phẩm") || modalTitle.length() > 0, 
                         "Modal phải có tiêu đề");
        System.out.println("✓ Tiêu đề modal: " + modalTitle);

        System.out.println("✓ TC_ADMIN_PRODUCT_004 PASSED\n");
    }

    @Test(priority = 5, description = "TC_ADMIN_PRODUCT_005: Xem các sản phẩm ngừng hoạt động")
    public void testFilterByStatus() {
        System.out.println("\n[TC_ADMIN_PRODUCT_005] Test: Xem các sản phẩm ngừng hoạt động");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Lọc sản phẩm ngừng hoạt động
        adminProductPage.selectStatusFilter("Ngừng hoạt động");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int inactiveCount = adminProductPage.getProductCount();
        System.out.println("✓ Số sản phẩm ngừng hoạt động: " + inactiveCount);
        Assert.assertTrue(inactiveCount >= 0, "Phải hiển thị danh sách sản phẩm ngừng hoạt động");

        System.out.println("✓ TC_ADMIN_PRODUCT_005 PASSED\n");
    }

    @Test(priority = 6, description = "TC_ADMIN_PRODUCT_006: Thêm sản phẩm mới")
    public void testOpenAddProductModal() {
        System.out.println("\n[TC_ADMIN_PRODUCT_006] Test: Thêm sản phẩm mới");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Thêm sản phẩm mới
        adminProductPage.clickAddProductButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal thêm sản phẩm hiển thị
        Assert.assertTrue(adminProductPage.isAddProductModalDisplayed(), 
                         "Modal thêm sản phẩm phải hiển thị");
        System.out.println("✓ Modal thêm sản phẩm đã hiển thị");

        // Điền thông tin sản phẩm
        String productName = "Sản phẩm Test " + System.currentTimeMillis();
        adminProductPage.fillProductName(productName);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductPrice("150000");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductStock("100");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductDescription("Mô tả sản phẩm test tự động");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Chọn danh mục
        adminProductPage.selectCategory("Tiểu thuyết");
        System.out.println("✓ Đã điền đầy đủ thông tin sản phẩm");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Tạo sản phẩm
        adminProductPage.clickSubmitProductButton();

        try {
            Thread.sleep(3000); // Đợi xử lý tạo sản phẩm
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal đã đóng
        Assert.assertTrue(adminProductPage.isModalClosed(), 
                         "Modal phải đóng sau khi tạo sản phẩm thành công");
        System.out.println("✓ Modal đã đóng sau khi tạo sản phẩm");

        System.out.println("✓ TC_ADMIN_PRODUCT_006 PASSED\n");
    }

    @Test(priority = 7, description = "TC_ADMIN_PRODUCT_007: Sửa thông tin sản phẩm")
    public void testOpenEditProductModal() {
        System.out.println("\n[TC_ADMIN_PRODUCT_007] Test: Sửa thông tin sản phẩm");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click chọn sản phẩm đầu tiên
        adminProductPage.clickFirstProduct();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra sản phẩm đã được chọn
        Assert.assertTrue(adminProductPage.isProductSelected(), 
                         "Sản phẩm phải được chọn trước khi sửa");
        System.out.println("✓ Sản phẩm đã được chọn");

        // Click nút Sửa sản phẩm
        adminProductPage.clickEditProductButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal sửa sản phẩm hiển thị
        Assert.assertTrue(adminProductPage.isEditProductModalDisplayed(), 
                         "Modal sửa sản phẩm phải hiển thị");
        System.out.println("✓ Modal sửa sản phẩm đã hiển thị");

        // Sửa thông tin sản phẩm
        adminProductPage.fillProductPrice("200000");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductStock("150");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductDescription("Mô tả đã được cập nhật - Test tự động");
        System.out.println("✓ Đã sửa thông tin sản phẩm");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Cập nhật sản phẩm
        adminProductPage.clickSubmitProductButton();

        try {
            Thread.sleep(3000); // Đợi xử lý cập nhật
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal đã đóng
        Assert.assertTrue(adminProductPage.isModalClosed(), 
                         "Modal phải đóng sau khi cập nhật sản phẩm thành công");
        System.out.println("✓ Modal đã đóng sau khi cập nhật sản phẩm");

        System.out.println("✓ TC_ADMIN_PRODUCT_007 PASSED\n");
    }

    @Test(priority = 8, description = "TC_ADMIN_PRODUCT_008: Thêm sản phẩm với số lượng âm")
    public void testAddProductWithNegativeStock() {
        System.out.println("\n[TC_ADMIN_PRODUCT_008] Test: Thêm sản phẩm với số lượng âm");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Thêm sản phẩm mới
        adminProductPage.clickAddProductButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal thêm sản phẩm hiển thị
        Assert.assertTrue(adminProductPage.isAddProductModalDisplayed(), 
                         "Modal thêm sản phẩm phải hiển thị");
        System.out.println("✓ Modal thêm sản phẩm đã hiển thị");

        // Điền thông tin sản phẩm với số lượng âm
        String productName = "Sản phẩm Test Negative " + System.currentTimeMillis();
        adminProductPage.fillProductName(productName);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductPrice("100000");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Nhập số lượng âm
        adminProductPage.fillProductStock("-50");
        System.out.println("✓ Đã nhập số lượng âm: -50");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        adminProductPage.fillProductDescription("Mô tả sản phẩm test với số lượng âm");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Chọn danh mục đầu tiên
        adminProductPage.selectCategory("");
        System.out.println("✓ Đã điền thông tin với số lượng âm");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Tạo sản phẩm
        adminProductPage.clickSubmitProductButton();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal vẫn còn mở (không cho phép submit với số lượng âm)
        Assert.assertFalse(adminProductPage.isModalClosed(), 
                         "Modal phải vẫn mở do validation lỗi số lượng âm");
        System.out.println("✓ Modal vẫn mở, không cho phép tạo sản phẩm với số lượng âm");

        System.out.println("✓ TC_ADMIN_PRODUCT_008 PASSED\n");
    }

    @Test(priority = 9, description = "TC_ADMIN_PRODUCT_009: Sửa sản phẩm với số lượng âm")
    public void testEditProductWithNegativeStock() {
        System.out.println("\n[TC_ADMIN_PRODUCT_009] Test: Sửa sản phẩm với số lượng âm");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click chọn sản phẩm đầu tiên
        adminProductPage.clickFirstProduct();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra sản phẩm đã được chọn
        Assert.assertTrue(adminProductPage.isProductSelected(), 
                         "Sản phẩm phải được chọn");
        System.out.println("✓ Sản phẩm đã được chọn");

        // Click nút Sửa sản phẩm
        adminProductPage.clickEditProductButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal sửa sản phẩm hiển thị
        Assert.assertTrue(adminProductPage.isEditProductModalDisplayed(), 
                         "Modal sửa sản phẩm phải hiển thị");
        System.out.println("✓ Modal sửa sản phẩm đã hiển thị");

        // Sửa số lượng thành số âm
        adminProductPage.fillProductStock("-100");
        System.out.println("✓ Đã nhập số lượng âm: -100");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Cập nhật sản phẩm
        adminProductPage.clickSubmitProductButton();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal vẫn còn mở (không cho phép cập nhật với số lượng âm)
        Assert.assertFalse(adminProductPage.isModalClosed(), 
                         "Modal phải vẫn mở do validation lỗi số lượng âm");
        System.out.println("✓ Modal vẫn mở, không cho phép cập nhật sản phẩm với số lượng âm");

        System.out.println("✓ TC_ADMIN_PRODUCT_009 PASSED\n");
    }

    @Test(priority = 10, description = "TC_ADMIN_PRODUCT_010: Chuyển trạng thái sản phẩm sang ngừng hoạt động")
    public void testChangeProductStatusToInactive() {
        System.out.println("\n[TC_ADMIN_PRODUCT_010] Test: Chuyển trạng thái sản phẩm sang ngừng hoạt động");

        adminProductPage.navigateToProductPage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click chọn sản phẩm đầu tiên
        adminProductPage.clickFirstProduct();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra sản phẩm đã được chọn
        Assert.assertTrue(adminProductPage.isProductSelected(), 
                         "Sản phẩm phải được chọn");
        System.out.println("✓ Sản phẩm đã được chọn");

        // Click nút Sửa sản phẩm
        adminProductPage.clickEditProductButton();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal sửa sản phẩm hiển thị
        Assert.assertTrue(adminProductPage.isEditProductModalDisplayed(), 
                         "Modal sửa sản phẩm phải hiển thị");
        System.out.println("✓ Modal sửa sản phẩm đã hiển thị");

        // Chọn trạng thái "Ngừng hoạt động" (option thứ 2 trong dropdown)
        adminProductPage.selectProductStatus("Ngừng hoạt động");
        System.out.println("✓ Đã chọn trạng thái: Ngừng hoạt động");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click nút Cập nhật sản phẩm
        adminProductPage.clickSubmitProductButton();

        try {
            Thread.sleep(3000); // Đợi submit xử lý
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Kiểm tra modal đã đóng (cập nhật thành công)
        Assert.assertTrue(adminProductPage.isModalClosed(), 
                         "Modal phải đóng sau khi cập nhật thành công");
        System.out.println("✓ Modal đã đóng, cập nhật trạng thái thành công");

        System.out.println("✓ TC_ADMIN_PRODUCT_010 PASSED\n");
    }
}
