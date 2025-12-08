package com.fruitshop.tests;

import com.fruitshop.pages.ProductPage;
import com.fruitshop.tests.base.BaseTest;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class ProductFilterTest extends BaseTest {
    private ProductPage productPage;

    @BeforeMethod
    public void setUpProductTest() {
        driver.get(baseUrl);
        productPage = new ProductPage(driver);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        productPage.navigateToProductPage();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1, testName = "TC_FILTER_001", description = "Lọc sản phẩm theo danh mục")
    public void testFilterByCategory() {
        System.out.println("[TC_FILTER_001] Test: Lọc sản phẩm theo danh mục");
        
        List<WebElement> categories = productPage.getCategoryItems();
        Assert.assertTrue(categories.size() > 0, "Không có danh mục nào");
        
        String firstCategoryName = categories.get(0).getText();
        System.out.println("Đang lọc theo danh mục: " + firstCategoryName);
        
        productPage.clickCategoryByName(firstCategoryName);
        
        int productCount = productPage.getProductCount();
        Assert.assertTrue(productCount > 0, "Không có sản phẩm nào hiển thị sau khi lọc");
        
        System.out.println("✓ Lọc theo danh mục thành công, hiển thị " + productCount + " sản phẩm");
    }

    @Test(priority = 2, testName = "TC_FILTER_002", description = "Lọc sản phẩm theo giá dưới 200k")
    public void testFilterByPriceUnder200k() {
        System.out.println("[TC_FILTER_002] Test: Lọc sản phẩm theo giá dưới 200k");
        
        // Chọn danh mục trước vì bộ lọc giá chỉ hiển thị khi đã chọn danh mục
        List<WebElement> categories = productPage.getCategoryItems();
        if (categories.size() > 0) {
            String categoryName = categories.get(0).getText();
            productPage.clickCategoryByName(categoryName);
        }
        
        productPage.clickPriceFilterUnder200k();
        
        boolean isClearFilterDisplayed = productPage.isClearFilterButtonDisplayed();
        Assert.assertTrue(isClearFilterDisplayed, "Nút 'Xóa bộ lọc' không hiển thị");
        
        int productCount = productPage.getProductCount();
        if (productCount > 0) {
            boolean allPricesInRange = productPage.areAllProductPricesInRange(0, 200000);
            Assert.assertTrue(allPricesInRange, "Có sản phẩm có giá ngoài khoảng dưới 200k");
            System.out.println("✓ Lọc giá dưới 200k thành công, hiển thị " + productCount + " sản phẩm");
        } else {
            System.out.println("✓ Không có sản phẩm nào trong khoảng giá này");
        }
    }


    @Test(priority = 4, testName = "TC_FILTER_004", description = "Xóa bộ lọc giá")
    public void testClearPriceFilter() {
        System.out.println("[TC_FILTER_004] Test: Xóa bộ lọc giá");
        
        // Chọn danh mục trước
        List<WebElement> categories = productPage.getCategoryItems();
        if (categories.size() > 0) {
            String categoryName = categories.get(0).getText();
            productPage.clickCategoryByName(categoryName);
        }
        
        productPage.clickPriceFilterUnder200k();
        
        boolean isClearFilterDisplayed = productPage.isClearFilterButtonDisplayed();
        Assert.assertTrue(isClearFilterDisplayed, "Nút 'Xóa bộ lọc' không hiển thị");
        
        int filteredProductCount = productPage.getProductCount();
        
        productPage.clickClearFilter();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        boolean isClearFilterHidden = !productPage.isClearFilterButtonDisplayed();
        Assert.assertTrue(isClearFilterHidden, "Nút 'Xóa bộ lọc' vẫn hiển thị sau khi xóa");
        
        int allProductCount = productPage.getProductCount();
        Assert.assertTrue(allProductCount >= filteredProductCount, 
            "Số sản phẩm sau khi xóa bộ lọc ít hơn khi có bộ lọc");
        
        System.out.println("✓ Xóa bộ lọc thành công, hiển thị " + allProductCount + " sản phẩm");
    }

}
