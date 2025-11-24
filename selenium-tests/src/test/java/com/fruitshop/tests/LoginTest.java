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

    @Test(priority = 1, description = "Đăng nhập thành công với tài khoản Customer")
    public void testLoginSuccessAsCustomer() {
        System.out.println("Test: Đăng nhập với SĐT 0355142890");
        
        loginPage.login("0355142890", "123456");
        
        // Đợi chuyển trang
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra URL sau khi đăng nhập (điều chỉnh theo app của bạn)
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL sau đăng nhập: " + currentUrl);
        
        // Kiểm tra đã chuyển trang (không còn ở /login)
        Assert.assertFalse(currentUrl.contains("/login"), "Đăng nhập thất bại, vẫn ở trang login");
    }

    // Tạm thời comment các test khác, chỉ test Customer trước
    /*
    @Test(priority = 2, description = "Đăng nhập thành công với tài khoản Admin")
    public void testLoginSuccessAsAdmin() {
        System.out.println("Test: Đăng nhập với Admin 1");
        
        loginPage.login("0987654321", "123456");
        
        // Đợi chuyển trang
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL sau đăng nhập: " + currentUrl);
        
        // Kiểm tra đã chuyển trang
        Assert.assertFalse(currentUrl.contains("/login"), "Đăng nhập Admin thất bại");
    }

    @Test(priority = 3, description = "Đăng nhập thất bại với mật khẩu sai")
    public void testLoginWithWrongPassword() {
        System.out.println("Test: Đăng nhập với mật khẩu sai");
        
        loginPage.login("0123456789", "wrongpassword");
        
        // Đợi thông báo lỗi
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra vẫn ở trang login hoặc có thông báo lỗi
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL hiện tại: " + currentUrl);
        
        boolean hasError = loginPage.isErrorMessageDisplayed() || currentUrl.contains("/login");
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi nhập sai mật khẩu");
    }

    @Test(priority = 4, description = "Đăng nhập với số điện thoại không tồn tại")
    public void testLoginWithInvalidPhone() {
        System.out.println("Test: Đăng nhập với SĐT không tồn tại");
        
        loginPage.login("0999999999", "123456");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        boolean hasError = loginPage.isErrorMessageDisplayed() || currentUrl.contains("/login");
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi nhập SĐT không tồn tại");
    }

    @Test(priority = 5, description = "Đăng nhập với thông tin để trống")
    public void testLoginWithEmptyFields() {
        System.out.println("Test: Đăng nhập với thông tin trống");
        
        loginPage.clickLoginButton();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra vẫn ở trang login
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login"), "Không kiểm tra validation khi để trống");
    }
    */
}
