package com.fruitshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProductPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By viewAllProductsButton = By.xpath("//button[contains(text(), 'Xem tất cả sản phẩm')]");
    private By categoryItems = By.xpath("//div[contains(@class, 'cursor-pointer')]//span[contains(@class, 'text-gray-700')]");
    private By priceRangeUnder200k = By.xpath("//span[contains(text(), 'Dưới 200k')]");
    private By priceRange200to600k = By.xpath("//span[contains(text(), '200k - 600k')]");
    private By clearFilterButton = By.xpath("//button[contains(text(), 'Xóa bộ lọc')]");
    private By productItems = By.xpath("//div[contains(@class, 'grid')]//div[contains(@class, 'bg-white') and contains(@class, 'rounded-lg')]");
    private By productPrices = By.xpath("//p[contains(@class, 'text-primary') and contains(@class, 'font-bold')]");
    private By nextButton = By.xpath("//button[contains(., 'Sau')]");
    private By activePageButton = By.xpath("//button[contains(@class, 'bg-blue-600') and contains(@class, 'text-white')]");

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void navigateToProductPage() {
        try {
            WebElement viewAllButton = wait.until(ExpectedConditions.elementToBeClickable(viewAllProductsButton));
            viewAllButton.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            driver.get(driver.getCurrentUrl() + "/product");
        }
    }

    public void clickCategoryByName(String categoryName) {
        try {
            WebElement category = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(@class, 'text-gray-700') and contains(text(), '" + categoryName + "')]")
            ));
            category.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Không tìm thấy danh mục: " + categoryName);
        }
    }

    public void clickPriceFilterUnder200k() {
        try {
            WebElement priceFilter = wait.until(ExpectedConditions.elementToBeClickable(priceRangeUnder200k));
            priceFilter.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickPriceFilter200to600k() {
        try {
            WebElement priceFilter = wait.until(ExpectedConditions.elementToBeClickable(priceRange200to600k));
            priceFilter.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickClearFilter() {
        try {
            WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(clearFilterButton));
            clearButton.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickNextPage() {
        try {
            WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
            nextBtn.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<WebElement> getCategoryItems() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(categoryItems));
    }

    public int getProductCount() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productItems)).size();
    }

    public List<WebElement> getProductPrices() {
        return driver.findElements(productPrices);
    }

    public boolean isClearFilterButtonDisplayed() {
        try {
            return driver.findElement(clearFilterButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPaginationDisplayed() {
        try {
            return driver.findElement(nextButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getCurrentPageNumber() {
        try {
            String pageText = wait.until(ExpectedConditions.visibilityOfElementLocated(activePageButton)).getText();
            return Integer.parseInt(pageText);
        } catch (Exception e) {
            return 1;
        }
    }

    public int parsePriceToNumber(String priceText) {
        try {
            String cleanPrice = priceText.replace("đ", "").replace(".", "").replace(",", "").trim();
            return Integer.parseInt(cleanPrice);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean areAllProductPricesInRange(int minPrice, int maxPrice) {
        List<WebElement> prices = getProductPrices();
        for (WebElement priceElement : prices) {
            int price = parsePriceToNumber(priceElement.getText());
            if (price < minPrice || price > maxPrice) {
                System.out.println("Giá nằm ngoài khoảng: " + price);
                return false;
            }
        }
        return true;
    }
}
