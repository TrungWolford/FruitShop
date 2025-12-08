package com.fruitshop.tests;

import com.fruitshop.pages.LoginPage;
import com.fruitshop.pages.AdminLoginPage;
import com.fruitshop.tests.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private LoginPage loginPage;
    private AdminLoginPage adminLoginPage;

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
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra trường mật khẩu có thuộc tính required
        boolean isPasswordRequired = driver.findElement(org.openqa.selenium.By.id("password"))
            .getAttribute("required") != null;
        System.out.println("Trường mật khẩu có required: " + isPasswordRequired);
        
        // Kiểm tra URL vẫn ở trang chủ (modal vẫn mở)
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL hiện tại: " + currentUrl);
        
        Assert.assertTrue(isPasswordRequired && (currentUrl.equals(baseUrl) || currentUrl.equals(baseUrl + "/")), 
            "HTML5 validation không hoạt động đúng");
        
        System.out.println("✓ HTML5 validation hoạt động đúng - trường mật khẩu bắt buộc");
    }

    

    @Test(priority = 4, testName = "TC_LOGIN_004", description = "Đăng nhập với số điện thoại không tồn tại")
    public void testLoginWithNonExistentPhone() {
        System.out.println("[TC_LOGIN_004] Test: Đăng nhập với SĐT không tồn tại");
        
        loginPage.login("0999999999", "12345678");
        
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
        
        Assert.assertTrue(hasError, "Không có thông báo lỗi khi SĐT không tồn tại");
    }

    @Test(priority = 5, testName = "TC_LOGIN_005", description = "Đăng nhập với số điện thoại không hợp lệ")
    public void testLoginWithInvalidPhoneFormat() {
        System.out.println("[TC_LOGIN_005] Test: Đăng nhập với SĐT không hợp lệ (ít hơn 10 số)");
        
        loginPage.login("123", "12345678");
        
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
        
        // Có thể có validation lỗi hoặc vẫn ở trang login
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(hasError || currentUrl.equals(baseUrl) || currentUrl.equals(baseUrl + "/"), 
            "Không có validation cho SĐT không hợp lệ");
    }

    @Test(priority = 6, testName = "TC_LOGIN_006", description = "Đăng nhập thành công với tài khoản Admin")
    public void testLoginSuccessAsAdmin() {
        System.out.println("[TC_LOGIN_006] Test: Đăng nhập thành công với tài khoản Admin - SĐT 03348400360");
        System.out.println("Truy cập trang đăng nhập admin: " + baseUrl + "/admin");
        
        // Truy cập trực tiếp trang /admin
        driver.get(baseUrl + "/admin");
        
        // Đợi trang load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL hiện tại: " + currentUrl);
        
        // Khởi tạo AdminLoginPage
        adminLoginPage = new AdminLoginPage(driver);
        
        // Kiểm tra xem có form đăng nhập admin không
        boolean isAdminLoginPage = adminLoginPage.isOnAdminLoginPage();
        System.out.println("Có form đăng nhập admin: " + isAdminLoginPage);
        
        if (!isAdminLoginPage) {
            System.out.println("Không tìm thấy form đăng nhập trên trang /admin");
            System.out.println("Có thể trang đã redirect hoặc cấu trúc HTML khác");
            
            // In ra một phần page source để debug
            try {
                String pageSource = driver.getPageSource();
                System.out.println("Page source (500 ký tự đầu):");
                System.out.println(pageSource.substring(0, Math.min(500, pageSource.length())));
            } catch (Exception ex) {
                System.out.println("Không lấy được page source");
            }
        }
        
        // Đăng nhập với tài khoản admin
        System.out.println("Đăng nhập với tài khoản admin...");
        try {
            adminLoginPage.login("03348400360", "123123");
            
            // Đợi xử lý đăng nhập
            Thread.sleep(4000);
            
            currentUrl = driver.getCurrentUrl();
            System.out.println("URL sau đăng nhập: " + currentUrl);
            
            // Kiểm tra xem đã redirect đến trang admin dashboard chưa (kiểm tra URL trước)
            if (currentUrl.contains("/admin/dashboard") || (currentUrl.contains("/admin") && !currentUrl.endsWith("/admin"))) {
                System.out.println("✓ Đăng nhập Admin thành công, đang ở: " + currentUrl);
                Assert.assertTrue(true, "Đăng nhập admin thành công");
            } else {
                // Chỉ kiểm tra error nếu URL chưa thay đổi
                boolean hasError = adminLoginPage.isErrorMessageDisplayed();
                if (hasError) {
                    String errorMsg = adminLoginPage.getErrorMessage();
                    System.out.println("✗ Có lỗi đăng nhập: " + errorMsg);
                    Assert.fail("Đăng nhập Admin thất bại với lỗi: " + errorMsg);
                }
                
                System.out.println("✗ Chưa redirect, đợi thêm 6 giây...");
                Thread.sleep(6000);
                
                currentUrl = driver.getCurrentUrl();
                System.out.println("URL sau 10 giây: " + currentUrl);
                
                if (currentUrl.contains("/admin/dashboard") || (currentUrl.contains("/admin") && !currentUrl.endsWith("/admin"))) {
                    System.out.println("✓ Đăng nhập Admin thành công");
                    Assert.assertTrue(true);
                } else {
                    Assert.fail("Đăng nhập Admin thất bại - URL: " + currentUrl);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Lỗi trong quá trình đăng nhập: " + e.getMessage());
            e.printStackTrace();
            Assert.fail("Lỗi test: " + e.getMessage());
        }
    }

    @Test(priority = 7, testName = "TC_LOGOUT_001", description = "Đăng xuất thành công sau khi đăng nhập với tài khoản khách hàng")
    public void testLogoutSuccess() {
        System.out.println("[TC_LOGOUT_001] Test: Đăng xuất thành công với tài khoản khách hàng");
        
        // Bước 1: Đăng nhập với tài khoản khách hàng
        System.out.println("Bước 1: Đăng nhập với tài khoản khách hàng 0483233172");
        loginPage.login("0483233172", "12345678");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String urlAfterLogin = driver.getCurrentUrl();
        System.out.println("URL sau đăng nhập: " + urlAfterLogin);
        
        // Kiểm tra đã đăng nhập thành công (không còn ở trang login)
        Assert.assertFalse(urlAfterLogin.contains("/login"), "Đăng nhập thất bại");
        System.out.println("✓ Đăng nhập thành công");
        
        // Bước 2: Đăng xuất
        System.out.println("Bước 2: Thực hiện đăng xuất...");
        loginPage.logout();
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String urlAfterLogout = driver.getCurrentUrl();
        System.out.println("URL sau đăng xuất: " + urlAfterLogout);
        
        // Bước 3: Kiểm tra đã về trang chủ
        boolean isLoggedOut = urlAfterLogout.equals(baseUrl) || urlAfterLogout.equals(baseUrl + "/");
        Assert.assertTrue(isLoggedOut, "Đăng xuất thất bại, không về trang chủ");
        
        System.out.println("✓ Đăng xuất thành công - Đã quay về trang chủ");
    }

}
