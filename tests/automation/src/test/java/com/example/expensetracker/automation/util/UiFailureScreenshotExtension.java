package com.example.expensetracker.automation.util;

import com.example.expensetracker.automation.config.TestConfig;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UiFailureScreenshotExtension implements AfterTestExecutionCallback {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }

        WebDriver driver = DriverManager.getDriverOrNull();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }

        String safeName = context.getDisplayName().replaceAll("[^A-Za-z0-9._-]", "_");
        Path destination = TestConfig.screenshotDirectory().resolve(
                safeName + "-" + LocalDateTime.now().format(FORMATTER) + ".png"
        );

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(
                    screenshotDriver.getScreenshotAs(OutputType.FILE).toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException ignored) {
            // A screenshot failure must not hide the original test failure.
        }
    }
}
