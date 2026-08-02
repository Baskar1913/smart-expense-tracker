package com.example.expensetracker.automation.ui.pages;

import com.example.expensetracker.automation.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(TestConfig.explicitWaitSeconds())
        );
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> all(By locator) {
        return driver.findElements(locator);
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected By buttonByText(String text) {
        return By.xpath("//button[normalize-space()='" + text + "']");
    }

    protected By inputInsideLabel(String label, int occurrence) {
        return By.xpath("(//label[contains(normalize-space(.),'" + label + "')]//input)[" + occurrence + "]");
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                element
        );
    }
    protected void setDateValue(
            By locator,
            LocalDate date
    ) {
        WebElement element = visible(locator);

        JavascriptExecutor javascript =
                (JavascriptExecutor) driver;

        javascript.executeScript(
                """
                const input = arguments[0];
                const value = arguments[1];

                const setter =
                    Object.getOwnPropertyDescriptor(
                        HTMLInputElement.prototype,
                        'value'
                    ).set;

                setter.call(input, value);

                input.dispatchEvent(
                    new Event('input', {
                        bubbles: true
                    })
                );

                input.dispatchEvent(
                    new Event('change', {
                        bubbles: true
                    })
                );
                """,
                element,
                date.toString()
        );

        String actual = element.getAttribute("value");
        if (!date.toString().equals(actual)) {
            throw new IllegalStateException(
                    "Date input did not keep the expected value. Expected "
                            + date + " but was " + actual
            );
        }
    }
}
