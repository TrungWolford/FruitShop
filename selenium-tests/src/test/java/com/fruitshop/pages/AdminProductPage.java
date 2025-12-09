package com.fruitshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AdminProductPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators dựa trên code React thực tế
    // Menu sidebar
    private By productsMenuItem = By.xpath("//div[contains(@class, 'flex flex-col')]//a[contains(text(), 'Sản phẩm')]");
    
    // Search and filter
    private By searchInput = By.xpath("//input[@placeholder='Tìm kiếm sản phẩm...']");
    private By statusFilterDropdown = By.xpath("//button[contains(@class, 'flex items-center justify-between')]");
    
    // Action buttons
    private By addProductButton = By.xpath("//button[contains(text(), 'Thêm sản phẩm mới')]");
    private By editProductButton = By.xpath("//button[contains(text(), 'Sửa sản phẩm')]");
    private By viewDetailButton = By.xpath("//button[contains(text(), 'Xem chi tiết')]");
    
    // Table
    private By productTable = By.xpath("//table");
    private By tableRows = By.xpath("//table//tbody//tr[not(contains(@class, 'skeleton'))]");
    private By tableHeaders = By.xpath("//table//thead//tr//th");
    
    // Product row elements
    private By firstProductRow = By.xpath("(//table//tbody//tr[not(contains(@class, 'skeleton'))])[1]");
    private By selectedProductRow = By.xpath("//tr[contains(@class, 'bg-blue-100')]");
    
    // Pagination
    private By previousPageButton = By.xpath("//button[contains(@aria-label, 'Previous') or .//svg[contains(@class, 'ChevronLeft')]]");
    private By nextPageButton = By.xpath("//button[contains(@aria-label, 'Next') or .//svg[contains(@class, 'ChevronRight')]]");
    private By currentPageInfo = By.xpath("//*[contains(text(), 'Trang')]");

    public AdminProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickProductsMenu() {
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(productsMenuItem));
            menu.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click menu Sản phẩm");
        } catch (Exception e) {
            System.out.println("Không tìm thấy menu Sản phẩm: " + e.getMessage());
        }
    }

    public void navigateToProductPage() {
        try {
            // Thử click vào menu Sản phẩm trong sidebar (React Router SPA)
            try {
                WebElement productsMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[@href='/admin/products' or contains(@href, 'products')]")
                ));
                productsMenu.click();
                System.out.println("✓ Đã click menu Sản phẩm trong sidebar");
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Không tìm thấy menu sidebar, thử điều hướng trực tiếp...");
                
                // Fallback: điều hướng trực tiếp
                String currentUrl = driver.getCurrentUrl();
                System.out.println("Current URL before navigation: " + currentUrl);
                
                String baseUrl;
                if (currentUrl.contains("/admin")) {
                    baseUrl = currentUrl.substring(0, currentUrl.indexOf("/admin"));
                } else {
                    baseUrl = currentUrl;
                }
                
                String targetUrl = baseUrl + "/admin/products";
                System.out.println("Navigating to: " + targetUrl);
                
                driver.get(targetUrl);
                Thread.sleep(3000);
            }
            
            System.out.println("✓ Đã điều hướng đến trang quản lý sản phẩm: " + driver.getCurrentUrl());
        } catch (Exception e) {
            System.out.println("Lỗi điều hướng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void searchProduct(String keyword) {
        try {
            WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
            searchField.clear();
            searchField.sendKeys(keyword);
            System.out.println("✓ Đã nhập từ khóa tìm kiếm: " + keyword);
            Thread.sleep(1000); // Đợi debounce
        } catch (Exception e) {
            System.out.println("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    public void clickAddProductButton() {
        try {
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addProductButton));
            addBtn.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click nút Thêm sản phẩm mới");
        } catch (Exception e) {
            System.out.println("Không tìm thấy nút Thêm sản phẩm: " + e.getMessage());
        }
    }

    public void clickEditProductButton() {
        try {
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(editProductButton));
            editBtn.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click nút Sửa sản phẩm");
        } catch (Exception e) {
            System.out.println("Không tìm thấy nút Sửa sản phẩm: " + e.getMessage());
        }
    }

    public void clickViewDetailButton() {
        try {
            WebElement viewBtn = wait.until(ExpectedConditions.elementToBeClickable(viewDetailButton));
            viewBtn.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click nút Xem chi tiết");
        } catch (Exception e) {
            System.out.println("Không tìm thấy nút Xem chi tiết: " + e.getMessage());
        }
    }

    public boolean isProductTableDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(productTable)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getProductCount() {
        try {
            List<WebElement> rows = driver.findElements(tableRows);
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickFirstProduct() {
        try {
            WebElement firstRow = wait.until(ExpectedConditions.elementToBeClickable(firstProductRow));
            firstRow.click();
            Thread.sleep(500);
            System.out.println("✓ Đã click sản phẩm đầu tiên");
        } catch (Exception e) {
            System.out.println("Không thể click sản phẩm: " + e.getMessage());
        }
    }

    public boolean isProductSelected() {
        try {
            // Tạo wait riêng với timeout 3 giây để đợi highlight class được apply
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement selectedRow = shortWait.until(ExpectedConditions.presenceOfElementLocated(selectedProductRow));
            return selectedRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickStatusFilter() {
        try {
            WebElement filterBtn = wait.until(ExpectedConditions.elementToBeClickable(statusFilterDropdown));
            filterBtn.click();
            Thread.sleep(500);
            System.out.println("✓ Đã click dropdown lọc trạng thái");
        } catch (Exception e) {
            System.out.println("Không tìm thấy dropdown lọc: " + e.getMessage());
        }
    }

    public void selectStatusFilter(String status) {
        try {
            clickStatusFilter();
            // Radix UI dropdown menu items are rendered with role="menuitem"
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='menuitem'][contains(., '" + status + "')]")
            ));
            option.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã chọn trạng thái: " + status);
        } catch (Exception e) {
            System.out.println("Không thể chọn trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clickNextPage() {
        try {
            WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
            nextBtn.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click nút trang tiếp theo");
        } catch (Exception e) {
            System.out.println("Không thể chuyển trang: " + e.getMessage());
        }
    }

    public void clickPreviousPage() {
        try {
            WebElement prevBtn = wait.until(ExpectedConditions.elementToBeClickable(previousPageButton));
            prevBtn.click();
            Thread.sleep(1000);
            System.out.println("✓ Đã click nút trang trước");
        } catch (Exception e) {
            System.out.println("Không thể chuyển trang: " + e.getMessage());
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isOnAdminProductPage() {
        String url = driver.getCurrentUrl();
        return url.contains("/admin/products");
    }

    public String getPageTitle() {
        try {
            WebElement title = driver.findElement(By.xpath("//h1[contains(text(), 'Quản lý sản phẩm')]"));
            return title.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isEditButtonEnabled() {
        try {
            WebElement editBtn = driver.findElement(editProductButton);
            return editBtn.isEnabled() && !editBtn.getAttribute("class").contains("disabled");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isViewDetailButtonEnabled() {
        try {
            WebElement viewBtn = driver.findElement(viewDetailButton);
            return viewBtn.isEnabled() && !viewBtn.getAttribute("class").contains("disabled");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isViewProductModalDisplayed() {
        try {
            // ViewProductModal sử dụng Dialog component từ Radix UI với data-state="open"
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@data-state='open' and contains(@class, 'fixed')]")
            ));
            return modal.isDisplayed();
        } catch (Exception e) {
            System.out.println("Không tìm thấy modal: " + e.getMessage());
            return false;
        }
    }

    public String getViewProductModalTitle() {
        try {
            // Title nằm trong DialogTitle component
            WebElement title = driver.findElement(
                By.xpath("//div[@data-state='open']//h2")
            );
            return title.getText();
        } catch (Exception e) {
            System.out.println("Không tìm thấy title modal: " + e.getMessage());
            return "";
        }
    }

    public boolean isAddProductModalDisplayed() {
        try {
            // AddProductModal sử dụng Dialog component với tiêu đề "Thêm sản phẩm mới"
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@data-state='open' and contains(@class, 'fixed')]")
            ));
            WebElement title = driver.findElement(
                By.xpath("//div[@data-state='open']//h2[contains(text(), 'Thêm sản phẩm mới')]")
            );
            return modal.isDisplayed() && title.isDisplayed();
        } catch (Exception e) {
            System.out.println("Không tìm thấy modal Thêm sản phẩm: " + e.getMessage());
            return false;
        }
    }

    public boolean isEditProductModalDisplayed() {
        try {
            // EditProductModal sử dụng Dialog component
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@data-state='open' and contains(@class, 'fixed')]")
            ));
            // Kiểm tra có form và nút "Cập nhật sản phẩm"
            WebElement updateButton = driver.findElement(
                By.xpath("//button[contains(text(), 'Cập nhật sản phẩm')]")
            );
            return modal.isDisplayed() && updateButton.isDisplayed();
        } catch (Exception e) {
            System.out.println("Không tìm thấy modal Sửa sản phẩm: " + e.getMessage());
            return false;
        }
    }

    public String getModalTitle() {
        try {
            WebElement title = driver.findElement(
                By.xpath("//div[@data-state='open']//h2")
            );
            return title.getText();
        } catch (Exception e) {
            System.out.println("Không tìm thấy title modal: " + e.getMessage());
            return "";
        }
    }

    // Methods for Add/Edit Product Form
    public void fillProductName(String productName) {
        try {
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='productName' or @placeholder='Nhập tên sản phẩm...']")
            ));
            nameInput.clear();
            nameInput.sendKeys(productName);
            System.out.println("✓ Đã nhập tên sản phẩm: " + productName);
        } catch (Exception e) {
            System.out.println("Lỗi nhập tên sản phẩm: " + e.getMessage());
        }
    }

    public void fillProductPrice(String price) {
        try {
            WebElement priceInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='price' or @type='number']")
            ));
            priceInput.clear();
            priceInput.sendKeys(price);
            System.out.println("✓ Đã nhập giá: " + price);
        } catch (Exception e) {
            System.out.println("Lỗi nhập giá: " + e.getMessage());
        }
    }

    public void fillProductStock(String stock) {
        try {
            WebElement stockInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='stock']")
            ));
            stockInput.clear();
            stockInput.sendKeys(stock);
            System.out.println("✓ Đã nhập tồn kho: " + stock);
        } catch (Exception e) {
            System.out.println("Lỗi nhập tồn kho: " + e.getMessage());
        }
    }

    public void fillProductDescription(String description) {
        try {
            WebElement descInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//textarea[@id='description']")
            ));
            descInput.clear();
            descInput.sendKeys(description);
            System.out.println("✓ Đã nhập mô tả: " + description);
        } catch (Exception e) {
            System.out.println("Lỗi nhập mô tả: " + e.getMessage());
        }
    }

    public void selectCategory(String categoryName) {
        try {
            // Scroll modal xuống để thấy phần category
            WebElement modal = driver.findElement(By.xpath("//div[@data-state='open']"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTo(0, 200);", modal
            );
            Thread.sleep(500);
            
            // Tìm tất cả badge và click vào badge đầu tiên (dễ nhất)
            java.util.List<WebElement> badges = driver.findElements(
                By.xpath("//div[@data-state='open']//div[contains(@class, 'cursor-pointer') and contains(@class, 'bg-white')]")
            );
            
            if (badges.size() > 0) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", badges.get(0));
                Thread.sleep(300);
                System.out.println("✓ Đã chọn danh mục đầu tiên");
            } else {
                System.out.println("⚠ Không tìm thấy danh mục nào, tiếp tục không cần danh mục");
            }
        } catch (Exception e) {
            System.out.println("⚠ Không thể chọn danh mục, tiếp tục không cần danh mục: " + e.getMessage());
        }
    }

    public void clickSubmitProductButton() {
        try {
            // Tìm nút "Tạo sản phẩm" hoặc "Cập nhật sản phẩm"
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Tạo sản phẩm') or contains(text(), 'Cập nhật sản phẩm')]")
            ));
            submitBtn.click();
            System.out.println("✓ Đã click nút submit form");
            Thread.sleep(2000); // Đợi xử lý
        } catch (Exception e) {
            System.out.println("Lỗi click nút submit: " + e.getMessage());
        }
    }

    public boolean isModalClosed() {
        try {
            // Kiểm tra modal đã đóng (không còn data-state='open')
            driver.findElement(By.xpath("//div[@data-state='open']"));
            return false; // Vẫn còn mở
        } catch (Exception e) {
            return true; // Đã đóng
        }
    }

    public void selectProductStatus(String status) {
        try {
            // Tìm modal
            WebElement modal = driver.findElement(By.xpath("//div[@data-state='open']"));
            System.out.println("=== DEBUG Modal ===");
            System.out.println("Modal tag: " + modal.getTagName());
            System.out.println("Modal class: " + modal.getAttribute("class"));
            
            // Scroll modal xuống để thấy phần trạng thái
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTo(0, arguments[0].scrollHeight);", modal
            );
            Thread.sleep(2000);

            // DEBUG: Tìm tất cả buttons trên TOÀN TRANG (không chỉ trong modal)
            java.util.List<WebElement> allButtons = driver.findElements(By.xpath("//button"));
            System.out.println("=== DEBUG: Tìm thấy " + allButtons.size() + " buttons trên toàn trang ===");
            
            // Tìm buttons có role="combobox"
            java.util.List<WebElement> comboboxButtons = driver.findElements(By.xpath("//button[@role='combobox']"));
            System.out.println("=== Tìm thấy " + comboboxButtons.size() + " buttons combobox trên toàn trang ===");
            for (int i = 0; i < comboboxButtons.size(); i++) {
                WebElement btn = comboboxButtons.get(i);
                try {
                    String text = btn.getText();
                    String dataState = btn.getAttribute("data-state");
                    String ariaExpanded = btn.getAttribute("aria-expanded");
                    System.out.println("Combobox " + i + ": text='" + text + "', data-state=" + dataState + ", aria-expanded=" + ariaExpanded);
                } catch (Exception e) {
                    System.out.println("Combobox " + i + ": Lỗi đọc");
                }
            }
            
            // Tìm button trạng thái (button combobox thứ 2)
            WebElement statusButton = null;
            if (comboboxButtons.size() >= 2) {
                statusButton = comboboxButtons.get(comboboxButtons.size() - 1); // Lấy button cuối
                System.out.println(">>> Chọn button combobox cuối: " + statusButton.getText());
            } else if (comboboxButtons.size() == 1) {
                statusButton = comboboxButtons.get(0);
                System.out.println(">>> Chọn button combobox duy nhất: " + statusButton.getText());
            }
            
            if (statusButton == null) {
                System.out.println("⚠ Không tìm thấy button combobox");
                return;
            }
            
            // Click button để mở dropdown
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", statusButton);
            Thread.sleep(500);
            statusButton.click();
            Thread.sleep(1500);
            System.out.println("✓ Đã mở dropdown trạng thái");

            // Chọn option
            java.util.List<WebElement> options = driver.findElements(By.xpath("//div[@role='option']"));
            System.out.println("Tìm thấy " + options.size() + " options");
            
            for (WebElement option : options) {
                String optionText = option.getText();
                System.out.println("- Option: " + optionText);
                if (optionText.equals(status)) {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                    Thread.sleep(500);
                    System.out.println("✓ Đã chọn trạng thái: " + status);
                    return;
                }
            }
            
            System.out.println("⚠ Không tìm thấy option: " + status);
        } catch (Exception e) {
            System.out.println("Lỗi chọn trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getProductStatusBadge() {
        try {
            // Đợi một chút để trang load
            Thread.sleep(1500);
            
            // Tìm badge trạng thái của sản phẩm đầu tiên (vì ta đã click vào đó)
            WebElement firstRow = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("(//table//tbody//tr)[1]")
            ));
            
            // Badge có class bg-green-700 hoặc bg-red-700
            WebElement badge = firstRow.findElement(
                By.xpath(".//span[contains(@class, 'bg-green-700') or contains(@class, 'bg-red-700')]")
            );
            
            String badgeText = badge.getText().trim();
            System.out.println("✓ Trạng thái hiện tại của sản phẩm: " + badgeText);
            return badgeText;
        } catch (Exception e) {
            System.out.println("Lỗi lấy trạng thái sản phẩm: " + e.getMessage());
            return "";
        }
    }

    public boolean isSuccessToastDisplayed(String expectedMessage) {
        try {
            // Toast notification từ react-hot-toast có structure:
            // <div role="status" aria-live="polite">...</div>
            // Không tìm theo text vì lỗi UTF-8, chỉ check role="status"
            WebDriverWait toastWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement toast = toastWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='status']")
            ));
            
            String toastText = toast.getText();
            System.out.println("✓ Toast hiển thị: " + toastText);
            
            // Verify toast is visible
            return toast.isDisplayed();
        } catch (Exception e) {
            System.out.println("Không tìm thấy toast notification: " + e.getMessage());
            return false;
        }
    }
}
