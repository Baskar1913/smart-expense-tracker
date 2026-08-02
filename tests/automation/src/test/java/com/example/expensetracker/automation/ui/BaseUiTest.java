package com.example.expensetracker.automation.ui;

import com.example.expensetracker.automation.api.ExpenseTrackerApiClient;
import com.example.expensetracker.automation.util.DriverManager;
import com.example.expensetracker.automation.util.UiFailureScreenshotExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public abstract class BaseUiTest {

    @RegisterExtension
    static final UiFailureScreenshotExtension SCREENSHOTS =
            new UiFailureScreenshotExtension();

    protected WebDriver driver;
    protected ExpenseTrackerApiClient api;

    @BeforeEach
    void startBrowser() {
        driver = DriverManager.startDriver();
        api = new ExpenseTrackerApiClient();
    }

    protected void clearBrowserSession() {
        ((JavascriptExecutor) driver).executeScript("sessionStorage.clear(); localStorage.clear();");
    }

    @AfterEach
    void stopBrowser() {
        DriverManager.quitDriver();
    }
}
