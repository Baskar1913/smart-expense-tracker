package com.example.expensetracker.automation.ui.pages;

import com.example.expensetracker.automation.config.TestConfig;
import com.example.expensetracker.automation.model.TestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class LoginPage extends BasePage {

    private final By heading = By.xpath("//h1[normalize-space()='Smart Expense Tracker']");
    private final By usernameInput = By.cssSelector("input[autocomplete='username']");
    private final By currentPasswordInput = By.cssSelector("input[autocomplete='current-password']");
    private final By successAlert = By.cssSelector(".alert.success");
    private final By errorAlert = By.cssSelector(".alert.error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(TestConfig.uiBaseUrl());
        visible(heading);
        return this;
    }

    public boolean isDisplayed() {
        return !all(heading).isEmpty();
    }

    public String loginUsernameValue() {
        return visible(usernameInput).getAttribute("value");
    }

    public String loginPasswordValue() {
        return visible(currentPasswordInput).getAttribute("value");
    }

    public boolean containsDevelopmentAccountsText() {
        return driver.getPageSource().contains("Development accounts");
    }

    public void submitLogin(String username, String password) {
        type(usernameInput, username);
        type(currentPasswordInput, password);
        click(buttonByText("Sign in"));
    }

    public DashboardPage loginSuccessfully(String username, String password) {
        submitLogin(username, password);
        return new DashboardPage(driver).waitUntilLoaded();
    }

    public LoginPage openRegistration() {
        click(buttonByText("Create account"));
        visible(By.xpath("//h2[normalize-space()='Create account']"));
        return this;
    }

    public void register(TestUser user, String confirmPassword) {
        List<WebElement> usernameFields = all(By.cssSelector("input[autocomplete='username']"));
        usernameFields.get(0).sendKeys(user.username());
        type(By.cssSelector("input[autocomplete='email']"), user.email());
        List<WebElement> passwordFields = all(By.cssSelector("input[autocomplete='new-password']"));
        passwordFields.get(0).sendKeys(user.password());
        passwordFields.get(1).sendKeys(confirmPassword);
        click(buttonByText("Create account"));
    }

    public String successMessage() {
        return visible(successAlert).getText();
    }

    public String errorMessage() {
        return visible(errorAlert).getText();
    }

    public LoginPage openForgotPassword(String username) {
        type(usernameInput, username);
        click(buttonByText("Forgot password?"));
        visible(By.xpath("//h2[normalize-space()='Forgot password']"));
        return this;
    }

    public void checkForgotUsername() {
        click(buttonByText("Check username"));
    }

    public boolean accountNotFoundIsDisplayed() {
        return !all(By.xpath("//h2[normalize-space()='Account not found']")).isEmpty();
    }

    public boolean createAccountOptionIsDisplayed() {
        return !all(buttonByText("Create account")).isEmpty();
    }

    public void verifyRegisteredEmail(String email) {
        type(By.cssSelector("input[autocomplete='email']"), email);
        click(buttonByText("Verify identity"));
        visible(By.xpath("//h2[normalize-space()='Set new password']"));
    }

    public void resetPassword(String newPassword) {
        List<WebElement> fields = all(By.cssSelector("input[autocomplete='new-password']"));
        fields.get(0).sendKeys(newPassword);
        fields.get(1).sendKeys(newPassword);
        click(buttonByText("Reset password"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentPasswordInput));
    }

    public void waitForErrorContaining(String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(errorAlert, text));
    }
    public LoginPage waitUntilLoaded() {
        wait.until(driver -> isDisplayed());
        return this;
    }
}
