package com.fruitshop.tests;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.fruitshop.pages.CartPage;
import com.fruitshop.pages.LoginPage;
import com.fruitshop.tests.base.BaseTest;

public class CartTest extends BaseTest {
    private CartPage cartPage;
    private LoginPage loginPage;

    @BeforeMethod
    public void setUpCartTest() {
        // BaseTest đã setup WebDriver, chỉ cần login
        driver.get("https://fruitshop-c4.vercel.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        loginPage = new LoginPage(driver);
        
        // Mở login modal và đăng nhập
        loginPage.openLoginModal();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        loginPage.login("0483233172", "12345678");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        cartPage = new CartPage(driver);
        
        // Xóa giỏ hàng cũ trong localStorage để đảm bảo test độc lập
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("localStorage.removeItem('cart');");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1, testName = "TC_CART_001", description = "Thêm sản phẩm từ trang chi tiết")
    public void testAddProductFromDetailPage() {
        System.out.println("[TC_CART_001] Test: Thêm sản phẩm từ trang chi tiết với số lượng 5");
        
        // Thêm sản phẩm với số lượng 5
        cartPage.addProductFromDetailPage("836a0bbf-2452-4fab-b873-744a3dfcedca", 5);
        
        // Mở giỏ hàng
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra giỏ hàng có sản phẩm
        Assert.assertTrue(cartPage.isCartOpen(), "Giỏ hàng không mở được");
        Assert.assertFalse(cartPage.isCartEmpty(), "Giỏ hàng trống");
        
        int itemCount = cartPage.getNumberOfCartItems();
        System.out.println("Số sản phẩm trong giỏ: " + itemCount);
        Assert.assertTrue(itemCount > 0, "Giỏ hàng không có sản phẩm");
        
        cartPage.closeCart();
        System.out.println("[TC_CART_001] Test PASSED: Đã thêm sản phẩm thành công");
    }

    @Test(priority = 2, testName = "TC_CART_002", description = "Thêm sản phẩm vào giỏ từ trang Home")
    public void testAddProductFromHomePage() {
        System.out.println("[TC_CART_002] Test: Thêm sản phẩm vào giỏ từ trang Home");
        
        // Thêm sản phẩm từ trang home
        cartPage.addProductToCart("3");
        
        // Mở giỏ hàng
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra giỏ hàng có sản phẩm
        Assert.assertTrue(cartPage.isCartOpen(), "Giỏ hàng không mở được");
        int itemCount = cartPage.getNumberOfCartItems();
        System.out.println("Số sản phẩm trong giỏ: " + itemCount);
        Assert.assertTrue(itemCount > 0, "Giỏ hàng không có sản phẩm sau khi thêm");
        
        cartPage.closeCart();
        System.out.println("[TC_CART_002] Test PASSED: Đã thêm sản phẩm từ Home thành công");
    }

    @Test(priority = 3, testName = "TC_CART_003", description = "Tăng số lượng sản phẩm trong giỏ")
    public void testIncreaseProductQuantity() {
        System.out.println("[TC_CART_003] Test: Tăng số lượng sản phẩm đầu tiên trong giỏ");
        
        // Mở giỏ hàng (sản phẩm đã có từ test trước)
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Lấy số sản phẩm trong giỏ
        int itemCount = cartPage.getNumberOfCartItems();
        System.out.println("Số sản phẩm trong giỏ: " + itemCount);
        
        if (itemCount == 0) {
            System.out.println("Giỏ hàng trống, thêm sản phẩm mới");
            cartPage.closeCart();
            cartPage.addProductToCart("1");
            cartPage.openCart();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Tăng số lượng sản phẩm cuối cùng (vì item thay đổi sẽ xuống cuối)
        int totalItems = cartPage.getNumberOfCartItems();
        int itemIndex = totalItems - 1; // Item cuối cùng
        System.out.println("Tổng số sản phẩm: " + totalItems + ", thao tác với item index: " + itemIndex);
        
        // Lấy số lượng ban đầu
        int initialQuantity = cartPage.getProductQuantityInCart(itemIndex);
        System.out.println("Số lượng ban đầu của sản phẩm cuối cùng: " + initialQuantity);
        
        // Tăng số lượng
        cartPage.increaseQuantity(itemIndex);
        
        // Re-open cart để refresh data
        cartPage.reopenCart();
        
        // Sau khi tăng, item vẫn ở cuối
        int newQuantity = cartPage.getProductQuantityInCart(itemIndex);
        System.out.println("Số lượng sau khi tăng: " + newQuantity);
        Assert.assertEquals(newQuantity, initialQuantity + 1, "Số lượng không tăng đúng");
        
        cartPage.closeCart();
        System.out.println("[TC_CART_003] Test PASSED: Đã tăng số lượng sản phẩm cuối cùng thành công");
    }

    @Test(priority = 4, testName = "TC_CART_004", description = "Giảm số lượng sản phẩm trong giỏ")
    public void testDecreaseProductQuantity() {
        System.out.println("[TC_CART_004] Test: Giảm số lượng sản phẩm cuối cùng trong giỏ");
        
        // Mở giỏ hàng (sản phẩm đã có từ test trước)
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Giảm số lượng sản phẩm cuối cùng (vì item thay đổi sẽ xuống cuối)
        int totalItems = cartPage.getNumberOfCartItems();
        int itemIndex = totalItems - 1; // Item cuối cùng
        System.out.println("Tổng số sản phẩm: " + totalItems + ", thao tác với item index: " + itemIndex);
        
        // Lấy số lượng ban đầu
        int initialQuantity = cartPage.getProductQuantityInCart(itemIndex);
        System.out.println("Số lượng ban đầu: " + initialQuantity);
        
        // Giảm số lượng
        cartPage.decreaseQuantity(itemIndex);
        
        // Re-open cart để refresh data
        cartPage.reopenCart();
        
        // Sau khi giảm, item vẫn ở cuối
        int newQuantity = cartPage.getProductQuantityInCart(itemIndex);
        System.out.println("Số lượng sau khi giảm: " + newQuantity);
        Assert.assertTrue(newQuantity < initialQuantity, "Số lượng không giảm");
        
        cartPage.closeCart();
        System.out.println("[TC_CART_004] Test PASSED: Đã giảm số lượng thành công");
    }

    @Test(priority = 5, testName = "TC_CART_005", description = "Xóa sản phẩm khỏi giỏ")
    public void testRemoveProductFromCart() {
        System.out.println("[TC_CART_005] Test: Xóa sản phẩm đầu tiên khỏi giỏ");
        
        // Mở giỏ hàng (sản phẩm đã có từ test trước)
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Lấy số lượng sản phẩm ban đầu
        int initialCount = cartPage.getNumberOfCartItems();
        System.out.println("Số sản phẩm ban đầu: " + initialCount);
        
        if (initialCount > 0) {
            // Xóa sản phẩm đầu tiên (index 0)
            int itemIndex = 0;
            cartPage.removeItem(itemIndex);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Kiểm tra số lượng sản phẩm sau xóa
            int finalCount = cartPage.getNumberOfCartItems();
            System.out.println("Số sản phẩm sau khi xóa: " + finalCount);
            Assert.assertEquals(finalCount, initialCount - 1, "Sản phẩm không được xóa");
        }
        
        cartPage.closeCart();
        System.out.println("[TC_CART_005] Test PASSED: Đã xóa sản phẩm thành công");
    }

    @Test(priority = 6, testName = "TC_CART_006", description = "Kiểm tra tổng tiền hiển thị trong giỏ")
    public void testCartTotalAmount() {
        System.out.println("[TC_CART_006] Test: Kiểm tra tổng tiền hiển thị trong giỏ");
        
        // Mở giỏ hàng
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra tổng tiền hiển thị
        String totalAmount = cartPage.getTotalAmount();
        System.out.println("Tổng tiền: " + totalAmount);
        
        Assert.assertFalse(totalAmount.isEmpty(), "Tổng tiền không hiển thị");
        Assert.assertTrue(totalAmount.contains("₫"), "Tổng tiền không đúng định dạng VND");
        
        cartPage.closeCart();
        System.out.println("[TC_CART_006] Test PASSED: Tổng tiền hiển thị đúng");
    }

    @Test(priority = 7, testName = "TC_CART_007", description = "Thêm sản phẩm ở trang chi tiết vượt số lượng tồn kho")
    public void testAddProductExceedingStock() {
        System.out.println("[TC_CART_007] Test: Thêm sản phẩm vượt số lượng tồn kho (10000 sản phẩm)");
        
        // Thêm sản phẩm với số lượng 10000 (vượt quá stock)
        cartPage.addProductFromDetailPage("836a0bbf-2452-4fab-b873-744a3dfcedca", 10000);
        
        // Mở giỏ hàng
        cartPage.openCart();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra giỏ hàng
        Assert.assertTrue(cartPage.isCartOpen(), "Giỏ hàng không mở được");
        
        // Lấy số lượng sản phẩm trong giỏ
        int itemCount = cartPage.getNumberOfCartItems();
        System.out.println("Số sản phẩm trong giỏ: " + itemCount);
        
        if (itemCount > 0) {
            // Kiểm tra sản phẩm cuối cùng (vừa thêm)
            int lastItemIndex = itemCount - 1;
            int quantity = cartPage.getProductQuantityInCart(lastItemIndex);
            System.out.println("Số lượng sản phẩm được thêm: " + quantity);
            
            // Hệ thống sẽ giới hạn quantity <= stock hoặc < 10000
            Assert.assertTrue(quantity < 10000, "Hệ thống không giới hạn số lượng tồn kho");
            System.out.println("[TC_CART_007] Test PASSED: Hệ thống giới hạn đúng số lượng tồn kho (số lượng thực tế: " + quantity + ")");
        } else {
            System.out.println("[TC_CART_007] Test PASSED: Hệ thống từ chối thêm sản phẩm do vượt quá stock");
        }
        
        cartPage.closeCart();
    }
}
