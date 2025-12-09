package com.fruitshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators - Đã chỉnh sửa theo HTML thực tế
    private By accountButton = By.xpath("//button[contains(., 'Tài khoản')]");
    private By phoneNumberInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton = By.xpath("//button[@type='submit']");
    // Thử nhiều locator cho error message
    private By errorMessage = By.xpath("//*[contains(@class, 'text-red-500') or contains(text(), 'Invalid') or contains(text(), 'Sai') or contains(text(), 'lỗi')]");
    
    // Register locators
    private By registerLink = By.xpath("//button[contains(text(), 'Đăng ký ngay') or contains(., 'register')]");
    private By registerFullNameInput = By.id("fullName");
    private By registerPhoneInput = By.id("phone");
    private By registerPasswordInput = By.id("password");
    private By registerConfirmPasswordInput = By.id("confirmPassword");
    private By acceptTermsCheckbox = By.id("acceptTerms");
    private By registerButton = By.xpath("//button[contains(text(), 'Đăng ký tài khoản')]");
    private By backToLoginButton = By.xpath("//button[contains(text(), 'Đăng nhập ngay')]");
    
    // Logout locators
    private By logoutButton = By.xpath("//button[contains(text(), 'Đăng xuất') or contains(., 'Logout')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openLoginModal() {
        WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(accountButton));
        accountBtn.click();
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
            // Đợi thông báo lỗi xuất hiện (tối đa 5 giây)
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
    
    // Register methods
    public void clickRegisterLink() {
        try {
            WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(registerLink));
            registerBtn.click();
        } catch (Exception e) {
            System.out.println("Không tìm thấy link đăng ký trong modal");
        }
    }
    
    public void enterRegisterFullName(String fullName) {
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(registerFullNameInput));
        nameField.clear();
        nameField.sendKeys(fullName);
    }
    
    public void enterRegisterPhone(String phone) {
        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(registerPhoneInput));
        phoneField.clear();
        phoneField.sendKeys(phone);
    }
    
    public void enterRegisterPassword(String password) {
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(registerPasswordInput));
        passwordField.clear();
        passwordField.sendKeys(password);
    }
    
    public void enterRegisterConfirmPassword(String confirmPassword) {
        WebElement confirmField = wait.until(ExpectedConditions.visibilityOfElementLocated(registerConfirmPasswordInput));
        confirmField.clear();
        confirmField.sendKeys(confirmPassword);
    }
    
    public void clickAcceptTerms() {
        WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(acceptTermsCheckbox));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }
    
    public void clickRegisterButton() {
        WebElement regButton = wait.until(ExpectedConditions.elementToBeClickable(registerButton));
        regButton.click();
    }
    
    public void register(String fullName, String phone, String password, String confirmPassword) {
        enterRegisterFullName(fullName);
        enterRegisterPhone(phone);
        enterRegisterPassword(password);
        enterRegisterConfirmPassword(confirmPassword);
        clickAcceptTerms();
        clickRegisterButton();
    }
    
    public boolean isRegisterButtonEnabled() {
        try {
            WebElement regButton = driver.findElement(registerButton);
            return regButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Logout methods
    public void logout() {
        try {
            // Click vào nút tài khoản để mở menu
            WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(accountButton));
            accountBtn.click();
            
            Thread.sleep(1000);
            
            // Click nút đăng xuất
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
            logoutBtn.click();
        } catch (Exception e) {
            System.out.println("Không tìm thấy nút đăng xuất: " + e.getMessage());
        }
    }
    
    public boolean isLogoutButtonDisplayed() {
        try {
            // Click vào nút tài khoản để mở menu
            WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(accountButton));
            accountBtn.click();
            
            Thread.sleep(500);
            
            // Kiểm tra nút đăng xuất có hiển thị không
            return driver.findElement(logoutButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
