package com.fruitshop.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By cartIcon = By.xpath("//div[contains(@class, 'cursor-pointer') and .//div[text()='Giỏ hàng']]");
    private By cartDialog = By.xpath("//div[contains(@class, 'fixed') and contains(@class, 'inset-0')]");
    private By emptyCartMessage = By.xpath("//h4[contains(text(), 'Giỏ hàng trống')]");
    // Cart items chứa productId trong span với class text-red-700
    private By cartItems = By.xpath("//div[contains(@class, 'flex') and contains(@class, 'items-start') and contains(@class, 'gap-4') and .//span[contains(@class, 'text-red-700')]]");
    private By toastNotification = By.xpath("//li[contains(@data-sonner-toast, '')]");
    private By quantityInput = By.xpath(".//input[@type='number']");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openCart() {
        try {
            // Đợi toast notification biến mất trước khi click
            Thread.sleep(2000);
            List<WebElement> toasts = driver.findElements(toastNotification);
            if (!toasts.isEmpty()) {
                Thread.sleep(2000); // Đợi thêm nếu toast vẫn còn
            }
            
            WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(cartIcon));
            cart.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(cartDialog));
        } catch (Exception e) {
            System.out.println("Không thể mở giỏ hàng: " + e.getMessage());
        }
    }

    public void closeCart() {
        try {
            // Click vào overlay để đóng giỏ hàng
            WebElement overlay = driver.findElement(By.xpath("//div[contains(@class, 'fixed') and contains(@class, 'inset-0') and contains(@class, 'bg-black')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlay);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Không thể đóng giỏ hàng hoàn toàn, tiếp tục");
        }
    }
    
    public void reopenCart() {
        try {
            // Đóng cart hiện tại
            closeCart();
            Thread.sleep(1000);
            
            // Mở lại cart để refresh data
            openCart();
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Không thể re-open cart: " + e.getMessage());
        }
    }

    public boolean isCartOpen() {
        try {
            return driver.findElements(cartDialog).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCartEmpty() {
        try {
            return driver.findElements(emptyCartMessage).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void addProductToCart(String productId) {
        try {
            // Scroll xuống để thấy danh sách sản phẩm
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 600)");
            Thread.sleep(1000);
            
            // Tìm tất cả button "THÊM VÀO GIỎ HÀNG" trên trang
            List<WebElement> addButtons = driver.findElements(
                By.xpath("//button[contains(., 'THÊM VÀO GIỎ HÀNG')]")
            );
            
            if (addButtons.isEmpty()) {
                System.out.println("Không tìm thấy button 'THÊM VÀO GIỎ HÀNG'");
                return;
            }
            
            // Chuyển đổi productId thành index (productId "3" -> index 2)
            int productIndex = Integer.parseInt(productId) - 1;
            
            if (productIndex >= 0 && productIndex < addButtons.size()) {
                WebElement addButton = addButtons.get(productIndex);
                
                // Scroll đến button
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addButton);
                Thread.sleep(500);
                
                // Wait và click
                wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
                Thread.sleep(3000); // Đợi toast và cart update
            } else {
                System.out.println("Product index " + productIndex + " nằm ngoài phạm vi (0-" + (addButtons.size()-1) + ")");
            }
            
        } catch (Exception e) {
            System.out.println("Lỗi thêm sản phẩm: " + e.getMessage());
        }
    }

    public void addProductFromDetailPage(String productId, int quantity) {
        try {
            // Điều hướng đến trang chi tiết sản phẩm
            driver.get("https://fruitshop-c4.vercel.app/product/" + productId);
            Thread.sleep(2000);
            
            // Scroll xuống để thấy phần thêm vào giỏ
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400)");
            Thread.sleep(500);
            
            // Tìm input số lượng
            WebElement quantityInputElement = wait.until(ExpectedConditions.presenceOfElementLocated(quantityInput));
            
            // Xóa giá trị cũ và nhập số lượng mới
            quantityInputElement.clear();
            quantityInputElement.sendKeys(String.valueOf(quantity));
            
            // Đợi giá trị được cập nhật
            wait.until(driver -> {
                String value = quantityInputElement.getAttribute("value");
                return value != null && value.equals(String.valueOf(quantity));
            });
            
            Thread.sleep(500);
            
            // Click button "THÊM VÀO GIỎ" (chữ hoa)
            WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'border-red-600') and .//span[text()='THÊM VÀO GIỎ']]")
            ));
            
            // Scroll đến button trước khi click
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addButton);
            Thread.sleep(500);
            
            addButton.click();
            
            // Đợi toast hiển thị
            Thread.sleep(3000);
            
        } catch (Exception e) {
            System.out.println("Lỗi thêm sản phẩm từ detail page: " + e.getMessage());
        }
    }

    public void increaseQuantity(int itemIndex) {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            if (itemIndex < items.size()) {
                WebElement item = items.get(itemIndex);
                
                // Tìm div quantity control chứa 2 button +/-
                WebElement quantityControl = item.findElement(
                    By.xpath(".//div[contains(@class, 'flex') and contains(@class, 'items-center') and contains(@class, 'gap-2')]")
                );
                
                // Trong div này, tìm tất cả button (chỉ có 2: minus và plus)
                List<WebElement> buttons = quantityControl.findElements(By.tagName("button"));
                
                System.out.println("Tìm thấy " + buttons.size() + " buttons trong quantity control");
                
                // Button Plus là button cuối cùng (button 1)
                if (buttons.size() >= 2) {
                    WebElement increaseBtn = buttons.get(1);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", increaseBtn);
                    Thread.sleep(300);
                    
                    // Click 1 lần duy nhất
                    increaseBtn.click();
                    System.out.println("Đã click button tăng số lượng");
                    Thread.sleep(3000); // Đợi cart update lâu hơn
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi tăng số lượng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void decreaseQuantity(int itemIndex) {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            if (itemIndex < items.size()) {
                WebElement item = items.get(itemIndex);
                
                // Tìm div quantity control chứa 2 button +/-
                WebElement quantityControl = item.findElement(
                    By.xpath(".//div[contains(@class, 'flex') and contains(@class, 'items-center') and contains(@class, 'gap-2')]")
                );
                
                // Trong div này, tìm tất cả button (chỉ có 2: minus và plus)
                List<WebElement> buttons = quantityControl.findElements(By.tagName("button"));
                
                System.out.println("Tìm thấy " + buttons.size() + " buttons trong quantity control");
                
                // Button Minus là button đầu tiên (button 0)
                if (buttons.size() >= 2) {
                    WebElement decreaseBtn = buttons.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", decreaseBtn);
                    Thread.sleep(300);
                    decreaseBtn.click();
                    System.out.println("Đã click button giảm số lượng");
                    Thread.sleep(3000); // Đợi cart update
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi giảm số lượng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeItem(int itemIndex) {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            if (itemIndex < items.size()) {
                WebElement item = items.get(itemIndex);
                // Button xóa có class w-8 h-8 và hover:text-red-500
                WebElement removeBtn = item.findElement(
                    By.xpath(".//button[contains(@class, 'w-8') and contains(@class, 'h-8') and contains(@class, 'hover:text-red-500')]")
                );
                wait.until(ExpectedConditions.elementToBeClickable(removeBtn)).click();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Lỗi xóa sản phẩm: " + e.getMessage());
        }
    }

    public int getNumberOfCartItems() {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getProductQuantityInCart(int itemIndex) {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            System.out.println("Số cart items tìm thấy: " + items.size());
            
            if (itemIndex < items.size()) {
                WebElement item = items.get(itemIndex);
                
                // Tìm div chứa buttons +/- (flex items-center gap-2)
                WebElement quantityControl = item.findElement(
                    By.xpath(".//div[contains(@class, 'flex') and contains(@class, 'items-center') and contains(@class, 'gap-2')]")
                );
                
                // Tìm span số lượng nằm giữa 2 button
                WebElement quantitySpan = quantityControl.findElement(
                    By.xpath(".//span[contains(@class, 'text-sm') and contains(@class, 'font-semibold')]")
                );
                
                String text = quantitySpan.getText().trim();
                System.out.println("Đọc được số lượng: " + text);
                
                if (text.matches("^\\d+$")) {
                    return Integer.parseInt(text);
                }
            }
            
            System.out.println("Không tìm thấy số lượng");
        } catch (Exception e) {
            System.out.println("Lỗi lấy số lượng: " + e.getMessage());
        }
        return 0;
    }

    public String getTotalAmount() {
        try {
            // Tổng tiền hiển thị trong span chứa ký tự ₫
            WebElement totalElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(text(), '₫')]")
            ));
            return totalElement.getText().trim();
        } catch (Exception e) {
            System.out.println("Lỗi lấy tổng tiền: " + e.getMessage());
            return "";
        }
    }
}
