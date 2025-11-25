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
    private By errorMessage = By.cssSelector(".text-red-500");

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
            return driver.findElement(errorMessage).isDisplayed();
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
}
