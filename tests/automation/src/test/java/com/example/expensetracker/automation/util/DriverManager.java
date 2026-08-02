package com.example.expensetracker.automation.util;

import com.example.expensetracker.automation.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver startDriver() {
        if (DRIVER.get() != null) {
            return DRIVER.get();
        }

        createDirectories();
        WebDriver driver = switch (TestConfig.browser()) {
            case "edge" -> new EdgeDriver(edgeOptions());
            case "firefox" -> new FirefoxDriver(firefoxOptions());
            case "chrome" -> new ChromeDriver(chromeOptions());
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + TestConfig.browser()
            );
        };

        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been started");
        }
        return driver;
    }

    public static WebDriver getDriverOrNull() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("download.default_directory", TestConfig.downloadDirectory().toString());
        preferences.put("download.prompt_for_download", false);
        preferences.put("download.directory_upgrade", true);
        preferences.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", preferences);
        options.addArguments("--disable-notifications", "--disable-popup-blocking", "--window-size=1440,1000");
        if (TestConfig.headless()) {
            options.addArguments("--headless=new", "--window-size=1440,1000");
        }
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("download.default_directory", TestConfig.downloadDirectory().toString());
        preferences.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", preferences);
        options.addArguments("--disable-notifications", "--disable-popup-blocking");
        if (TestConfig.headless()) {
            options.addArguments("--headless=new", "--window-size=1440,1000");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.dir", TestConfig.downloadDirectory().toString());
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "text/plain");
        if (TestConfig.headless()) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static void createDirectories() {
        try {
            Files.createDirectories(TestConfig.downloadDirectory());
            Files.createDirectories(TestConfig.screenshotDirectory());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create test output directories", ex);
        }
    }
}
