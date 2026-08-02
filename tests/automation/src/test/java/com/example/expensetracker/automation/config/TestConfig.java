package com.example.expensetracker.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties was not found");
            }
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load config.properties", ex);
        }
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration property: " + key);
        }
        return value.trim();
    }

    public static String uiBaseUrl() {
        return get("ui.base.url");
    }

    public static String apiBaseUrl() {
        return get("api.base.url");
    }

    public static String browser() {
        return get("browser").toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static int explicitWaitSeconds() {
        return Integer.parseInt(get("explicit.wait.seconds"));
    }

    public static int downloadWaitSeconds() {
        return Integer.parseInt(get("download.wait.seconds"));
    }

    public static Path downloadDirectory() {
        return Path.of(get("download.dir")).toAbsolutePath().normalize();
    }

    public static Path screenshotDirectory() {
        return Path.of(get("screenshot.dir")).toAbsolutePath().normalize();
    }
}
