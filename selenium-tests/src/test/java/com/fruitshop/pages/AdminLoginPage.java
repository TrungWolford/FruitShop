package com.fruitshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminLoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators cho trang admin login - cần xác định lại dựa trên HTML thực tế
    // Thử nhiều biến thể có thể của form đăng nhập admin
    private By phoneNumberInput = By.xpath("//input[@type='text' or @type='tel' or @name='phone' or @name='username' or @id='phone' or @id='username' or @placeholder='Số điện thoại' or @placeholder='Phone']");
    private By passwordInput = By.xpath("//input[@type='password' or @name='password' or @id='password']");
    private By loginButton = By.xpath("//button[@type='submit' or contains(text(), 'Đăng nhập') or contains(text(), 'Login')]");
    private By errorMessage = By.xpath("//*[contains(@class, 'error') or contains(@class, 'text-red') or contains(text(), 'lỗi') or contains(text(), 'Invalid')]");

    public AdminLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterPhoneNumber(String phone) {
        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(phoneNumberInput));
        phoneField.clear();
        phoneField.sendKeys(phone);
    }

    public void enterPassword(String password) {
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    public void clickLoginButton() {
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginBtn.click();
    }

    public void login(String phone, String password) {
        enterPhoneNumber(phone);
        enterPassword(password);
        clickLoginButton();
    }

    public boolean isErrorMessageDisplayed() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return error.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            return driver.findElement(errorMessage).getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean isOnAdminLoginPage() {
        // Kiểm tra URL chứa "/admin" và có form đăng nhập
        try {
            String url = driver.getCurrentUrl();
            boolean hasForm = driver.findElements(passwordInput).size() > 0;
            return url.contains("/admin") && hasForm;
        } catch (Exception e) {
            return false;
        }
    }
}
