package com.fruitshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProductDetailPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By productName = By.xpath("//h1[contains(@class, 'text-3xl') or contains(@class, 'text-4xl')]");
    private By productPrice = By.xpath("//p[contains(@class, 'text-3xl') and contains(@class, 'font-bold')]");
    private By productDescription = By.xpath("//div[contains(@class, 'text-gray-600')]//p");
    private By productImages = By.xpath("//img[@alt]");
    private By quantityInput = By.xpath("//input[@type='number']");
    private By decreaseQuantityButton = By.xpath("//button[.//svg[contains(@class, 'lucide-minus')]]");
    private By increaseQuantityButton = By.xpath("//button[.//svg[contains(@class, 'lucide-plus')]]");
    private By addToCartButton = By.xpath("//button[contains(., 'THÊM VÀO GIỎ') or contains(., 'THÊM')]");
    private By buyNowButton = By.xpath("//button[contains(., 'MUA NGAY')]");
    private By productCategory = By.xpath("//p[contains(text(), 'Danh mục:')]");
    private By productStock = By.xpath("//p[contains(text(), 'Kho:') or contains(text(), 'Số lượng:')]");
    private By backButton = By.xpath("//button[contains(., 'Quay lại') or .//svg]");
    
    // Rating section
    private By ratingStars = By.xpath("//div[contains(@class, 'flex')]//svg[contains(@class, 'lucide-star')]");
    private By ratingCount = By.xpath("//span[contains(text(), 'đánh giá')]");
    private By ratingComments = By.xpath("//div[contains(@class, 'bg-white')]//p[contains(@class, 'text-gray-700')]");

    public ProductDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Get product name
    public String getProductName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(productName)).getText();
    }

    // Get product price
    public String getProductPrice() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice)).getText();
    }

    // Get product description
    public String getProductDescription() {
        try {
            return driver.findElement(productDescription).getText();
        } catch (Exception e) {
            return "";
        }
    }

    // Check if product images are displayed
    public boolean areImagesDisplayed() {
        List<WebElement> images = driver.findElements(productImages);
        return images.size() > 0 && images.get(0).isDisplayed();
    }

    // Get number of product images
    public int getImageCount() {
        return driver.findElements(productImages).size();
    }

    // Get current quantity
    public int getQuantity() {
        try {
            String quantityText = driver.findElement(quantityInput).getAttribute("value");
            return Integer.parseInt(quantityText);
        } catch (Exception e) {
            // If no input found, try to find displayed quantity
            try {
                WebElement quantityDisplay = driver.findElement(By.xpath("//span[contains(@class, 'font-bold')]"));
                return Integer.parseInt(quantityDisplay.getText());
            } catch (Exception ex) {
                return 1;
            }
        }
    }

    // Increase quantity
    public void increaseQuantity() {
        wait.until(ExpectedConditions.elementToBeClickable(increaseQuantityButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Decrease quantity
    public void decreaseQuantity() {
        wait.until(ExpectedConditions.elementToBeClickable(decreaseQuantityButton)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Set quantity directly
    public void setQuantity(int quantity) {
        WebElement input = driver.findElement(quantityInput);
        input.clear();
        input.sendKeys(String.valueOf(quantity));
    }

    // Click add to cart button
    public void clickAddToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
    }

    // Click buy now button
    public void clickBuyNow() {
        wait.until(ExpectedConditions.elementToBeClickable(buyNowButton)).click();
    }

    // Check if add to cart button is displayed
    public boolean isAddToCartButtonDisplayed() {
        try {
            return driver.findElement(addToCartButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Check if buy now button is displayed
    public boolean isBuyNowButtonDisplayed() {
        try {
            return driver.findElement(buyNowButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Get rating stars count (filled stars)
    public int getRatingStarsCount() {
        try {
            List<WebElement> stars = driver.findElements(ratingStars);
            int filledStars = 0;
            for (WebElement star : stars) {
                String fill = star.getAttribute("fill");
                if (fill != null && !fill.equals("none")) {
                    filledStars++;
                }
            }
            return filledStars;
        } catch (Exception e) {
            return 0;
        }
    }

    // Get rating count text
    public String getRatingCountText() {
        try {
            return driver.findElement(ratingCount).getText();
        } catch (Exception e) {
            return "";
        }
    }

    // Check if rating section exists
    public boolean hasRatingSection() {
        try {
            return driver.findElements(ratingStars).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Get number of rating comments displayed
    public int getRatingCommentsCount() {
        return driver.findElements(ratingComments).size();
    }

    // Parse price to number
    public double parsePriceToNumber(String priceText) {
        String cleanPrice = priceText
                .replaceAll("[^0-9]", "")
                .trim();
        if (cleanPrice.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(cleanPrice);
    }

    // Wait for page to load
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productName));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
