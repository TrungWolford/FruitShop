package com.fruitshop.tests;

import com.fruitshop.pages.LoginPage;
import com.fruitshop.tests.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private LoginPage loginPage;

    @BeforeMethod
    public void setUpLoginTest() {
        // Điều hướng đến trang chủ
        driver.get(baseUrl);
        loginPage = new LoginPage(driver);
        
        // Đợi trang chủ load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Click vào nút "Tài khoản" để mở modal login
        loginPage.openLoginModal();
        
        // Đợi modal mở
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1, testName = "TC_LOGIN_001", description = "Đăng nhập thành công với thông tin hợp lệ")
    public void testLoginSuccessAsCustomer() {
        System.out.println("[TC_LOGIN_001] Test: Đăng nhập thành công - SĐT 0483233172");
        
        loginPage.login("0483233172", "12345678");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL sau đăng nhập: " + currentUrl);
        
        // Kiểm tra đã đăng nhập thành công
        Assert.assertFalse(currentUrl.contains("/login"), "Đăng nhập thất bại, vẫn ở trang login");
    }

    @Test(priority = 2, testName = "TC_LOGIN_002", description = "Đăng nhập thất bại - Sai mật khẩu")
    public void testLoginWithWrongPassword() {
        System.out.println("[TC_LOGIN_002] Test: Đăng nhập với mật khẩu sai");
        
        loginPage.login("0483233172", "SaiMatKhau123");
        
        // Tăng thời gian chờ để backend trả lỗi và Redux update
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        boolean hasError = loginPage.isErrorMessageDisplayed();
        System.out.println("Có thông báo lỗi: " + hasError);
        
        if (hasError) {
            System.out.println("Nội dung lỗi: " + loginPage.getErrorMessage());
        }
        
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi nhập sai mật khẩu");
    }

    @Test(priority = 3, testName = "TC_LOGIN_003", description = "Đăng nhập thất bại - Bỏ trống trường mật khẩu bắt buộc")
    public void testLoginWithEmptyPassword() {
        System.out.println("[TC_LOGIN_003] Test: Kiểm tra HTML5 validation - Bỏ trống mật khẩu");
        
        // Nhập SĐT nhưng không nhập mật khẩu
        loginPage.enterPhoneNumber("0483233172");
        
        // Thử click nút đăng nhập
        loginPage.clickLoginButton();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra vẫn ở modal login (không submit được do HTML5 validation)
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL hiện tại: " + currentUrl);
        
        // Kiểm tra form không submit được (vẫn ở trang chủ)
        Assert.assertTrue(currentUrl.equals(baseUrl) || currentUrl.equals(baseUrl + "/"), 
            "HTML5 validation không hoạt động - form đã submit được khi mật khẩu trống");
        
        System.out.println("✓ HTML5 validation hoạt động đúng - không cho submit khi mật khẩu trống");
    }
}
