package com.fruitshop.tests;

import com.fruitshop.pages.ProductDetailPage;
import com.fruitshop.pages.ProductPage;
import com.fruitshop.tests.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class ProductDetailTest extends BaseTest {
    private ProductPage productPage;
    private ProductDetailPage productDetailPage;
    private WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        productPage = new ProductPage(driver);
        productDetailPage = new ProductDetailPage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Điều hướng đến trang chủ
        driver.get(baseUrl);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1, description = "TC_PRODUCT_DETAIL_001: Xem chi tiết sản phẩm từ danh sách")
    public void testViewProductDetailFromList() {
        System.out.println("[TC_PRODUCT_DETAIL_001] Test: Xem chi tiết sản phẩm từ danh sách");

        // Bước 1: Người dùng truy cập trang danh sách sản phẩm
        productPage.navigateToProductPage();
        System.out.println("✓ Đã truy cập trang danh sách sản phẩm");

        // Bước 2: Chọn một sản phẩm (click vào tên hoặc hình ảnh)
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//div[contains(@class, 'bg-white') and contains(@class, 'rounded-lg')])[1]")
        ));
        
        String productNameFromList = firstProduct.findElement(By.xpath(".//h3[contains(@class, 'font-semibold')]")).getText();
        System.out.println("✓ Chọn sản phẩm: " + productNameFromList);
        
        firstProduct.click();

        // Bước 3: Hệ thống điều hướng sang trang chi tiết sản phẩm
        try {
            Thread.sleep(2000); // Đợi trang tải
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue(currentUrl.contains("/product/") || currentUrl.contains("/products/"), 
                             "URL phải chứa '/product/' hoặc '/products/'");
            System.out.println("✓ Hệ thống đã điều hướng sang trang chi tiết sản phẩm: " + currentUrl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✓ TC_PRODUCT_DETAIL_001 PASSED: Xem chi tiết sản phẩm thành công");
    }
}
