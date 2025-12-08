package com.fruitshop.tests;

import com.fruitshop.pages.LoginPage;
import com.fruitshop.tests.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RegisterTest extends BaseTest {
    private LoginPage loginPage;

    @BeforeMethod
    public void setUpRegisterTest() {
        // Điều hướng đến trang đăng ký
        driver.get(baseUrl + "/account/register");
        loginPage = new LoginPage(driver);
        
        // Đợi trang load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1, testName = "TC_REGISTER_001", description = "Đăng ký thành công với thông tin hợp lệ")
    public void testRegisterSuccess() {
        System.out.println("[TC_REGISTER_001] Test: Đăng ký thành công");
        
        // Tạo số điện thoại ngẫu nhiên 10 chữ số để tránh trùng
        long timestamp = System.currentTimeMillis();
        String randomPhone = "09" + String.format("%08d", timestamp % 100000000);
        
        System.out.println("Đang đăng ký với SĐT: " + randomPhone);
        loginPage.register("Nguyen Van A", randomPhone, "123456", "123456");
        
        try {
            Thread.sleep(8000); // Đợi xử lý đăng ký và redirect
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL sau đăng ký: " + currentUrl);
        
        // Kiểm tra đã chuyển về trang chủ (đăng ký thành công)
        Assert.assertTrue(
            currentUrl.equals(baseUrl) || currentUrl.equals(baseUrl + "/") || currentUrl.equals(baseUrl + "/#"),
            "Đăng ký không thành công - URL: " + currentUrl
        );
    }

    @Test(priority = 2, testName = "TC_REGISTER_002", description = "Đăng ký với số điện thoại đã tồn tại")
    public void testRegisterWithExistingPhone() {
        System.out.println("[TC_REGISTER_002] Test: Đăng ký với SĐT đã tồn tại");
        
        // Sử dụng số điện thoại đã có trong hệ thống
        loginPage.register("Test User", "0483233172", "123456", "123456");
        
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
        
        // Kiểm tra có thông báo lỗi hoặc vẫn ở trang register
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
            hasError || currentUrl.contains("/register"),
            "Không có thông báo lỗi khi SĐT đã tồn tại"
        );
    }

    @Test(priority = 3, testName = "TC_REGISTER_003", description = "Đăng ký với mật khẩu xác nhận không khớp")
    public void testRegisterWithMismatchPassword() {
        System.out.println("[TC_REGISTER_003] Test: Mật khẩu xác nhận không khớp");
        
        loginPage.register("Test User", "0987654321", "123456", "654321");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        boolean hasError = loginPage.isErrorMessageDisplayed();
        System.out.println("Có thông báo lỗi: " + hasError);
        
        if (hasError) {
            System.out.println("Nội dung lỗi: " + loginPage.getErrorMessage());
        }
        
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi mật khẩu không khớp");
    }

    @Test(priority = 4, testName = "TC_REGISTER_004", description = "Đăng ký với mật khẩu quá ngắn")
    public void testRegisterWithShortPassword() {
        System.out.println("[TC_REGISTER_004] Test: Mật khẩu quá ngắn (dưới 6 ký tự)");
        
        loginPage.register("Test User", "0987654322", "123", "123");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        boolean hasError = loginPage.isErrorMessageDisplayed();
        System.out.println("Có thông báo lỗi: " + hasError);
        
        if (hasError) {
            System.out.println("Nội dung lỗi: " + loginPage.getErrorMessage());
        }
        
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi mật khẩu quá ngắn");
    }

    @Test(priority = 5, testName = "TC_REGISTER_005", description = "Đăng ký bỏ trống trường bắt buộc")
    public void testRegisterWithEmptyFields() {
        System.out.println("[TC_REGISTER_005] Test: Bỏ trống tên tài khoản");
        
        loginPage.enterRegisterFullName("");
        loginPage.enterRegisterPhone("0987654323");
        loginPage.enterRegisterPassword("123456");
        loginPage.enterRegisterConfirmPassword("123456");
        loginPage.clickAcceptTerms();
        loginPage.clickRegisterButton();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL hiện tại: " + currentUrl);
        
        // Kiểm tra vẫn ở trang register (không submit được)
        Assert.assertTrue(currentUrl.contains("/register"), 
            "Form đã submit được khi bỏ trống trường bắt buộc");
        
        System.out.println("✓ Validation hoạt động - không cho submit khi thiếu thông tin");
    }

    @Test(priority = 6, testName = "TC_REGISTER_006", description = "Đăng ký với số điện thoại không hợp lệ")
    public void testRegisterWithInvalidPhone() {
        System.out.println("[TC_REGISTER_006] Test: Số điện thoại không hợp lệ");
        
        loginPage.register("Test User", "123", "123456", "123456");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        boolean hasError = loginPage.isErrorMessageDisplayed();
        System.out.println("Có thông báo lỗi: " + hasError);
        
        if (hasError) {
            System.out.println("Nội dung lỗi: " + loginPage.getErrorMessage());
        }
        
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi SĐT không hợp lệ");
    }
}
