package com.example.expensetracker.automation.ui.pages;

import com.example.expensetracker.automation.config.TestConfig;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class DashboardPage extends BasePage {

    private final By dashboardHeading = By.xpath("//h1[normalize-space()='Expense dashboard']");
    private final By addExpensePanel = By.xpath(
            "//section[contains(@class,'panel')][.//h2[normalize-space()='Add expense']]"
    );
    private final By monthlyPanel = By.xpath(
            "//section[contains(@class,'panel')][.//h2[normalize-space()='Monthly summary']]"
    );
    private final By expensesPanel = By.xpath(
            "//section[contains(@class,'panel')][.//h2[normalize-space()='Your expenses']]"
    );

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public DashboardPage waitUntilLoaded() {
        visible(dashboardHeading);
        return this;
    }

    public String displayedUsername() {
        List<WebElement> usernames = all(By.cssSelector(".username"));
        return usernames.isEmpty() ? "" : usernames.get(0).getText().trim();
    }
    
    public void waitForExpenseToDisappear(
            String title
    ) {
        wait.until(
                ExpectedConditions
                        .invisibilityOfElementLocated(
                                expenseRow(title)
                        )
        );
    }
    
    public void addExpense(String title, BigDecimal amount, String category, LocalDate date) {
        WebElement panel = visible(addExpensePanel);
        type(within(panel, "Title"), title);
        type(within(panel, "Amount"), amount.toPlainString());
        type(within(panel, "Category"), category);
        setDateValue(
            within(panel, "Date"),
            date
        );
        panel.findElement(By.xpath(".//button[normalize-space()='Add expense']")).click();
        wait.until(driver -> expenseIsVisible(title)
                || !panel.findElements(By.cssSelector(".alert.error")).isEmpty());
        if (!expenseIsVisible(title)) {
            String message = panel.findElement(By.cssSelector(".alert.error")).getText();
            throw new AssertionError("Expense was not created. UI message: " + message);
        }
    }

    public String addExpenseCategoryValue() {
        WebElement panel = visible(addExpensePanel);
        return panel.findElement(withinRelative("Category")).getAttribute("value");
    }

    public boolean hasTableHeader(String header) {
        return !all(By.xpath(
                "//section[.//h2[normalize-space()='Your expenses']]//th[normalize-space()='" + header + "']"
        )).isEmpty();
    }

    public void waitForExpense(String title) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(expenseRow(title)));
    }

    public boolean expenseIsVisible(String title) {
        return !all(expenseRow(title)).isEmpty();
    }

    public int visibleExpenseRowCount() {
        return all(By.xpath("//section[.//h2[normalize-space()='Your expenses']]//tbody/tr[td/button]")).size();
    }

    public void search(String query, String category, LocalDate from, LocalDate to) {
        WebElement panel = visible(expensesPanel);
        type(within(panel, "Search"), query == null ? "" : query);
        type(within(panel, "Category"), category == null ? "" : category);
        if (from != null) {
            setDateValue(
                within(panel, "From"),
                from
            );
        }
        if (to != null) {
            setDateValue(
                within(panel, "To"),
                to
            );
        }
        panel.findElement(By.xpath(".//button[normalize-space()='Search']")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class,'empty') and contains(.,'Loading expenses')]")
        ));
    }

    public String totalSpendText() {
        return visible(By.xpath(
                "//article[contains(@class,'metric-card')][span[normalize-space()='Total spend']]/strong"
        )).getText();
    }

    public Path loadAndDownloadMonthlySummary(int year, int month) {
        Path expected = TestConfig.downloadDirectory().resolve(
                "expense-summary-" + year + "-" + String.format("%02d", month) + ".txt"
        );
        try {
            Files.deleteIfExists(expected);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to clear an old downloaded summary", ex);
        }

        WebElement panel = visible(monthlyPanel);
        type(within(panel, "Year"), String.valueOf(year));
        type(within(panel, "Month"), String.valueOf(month));
        panel.findElement(By.xpath(".//button[normalize-space()='Load summary']")).click();

        long deadline = System.nanoTime() + Duration.ofSeconds(TestConfig.downloadWaitSeconds()).toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.exists(expected)) {
                return expected;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for summary download", ex);
            }
        }
        throw new AssertionError("Summary file was not downloaded: " + expected);
    }

    public void deleteExpense(String title) {
        WebElement row = visible(expenseRow(title));
        WebElement delete = row.findElement(By.xpath(".//button[normalize-space()='Delete']"));
        scrollIntoView(delete);
        delete.click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(expenseRow(title)));
    }
    
    private final By logoutButton =
            By.xpath("//button[normalize-space()='Log out']");

    public LoginPage logout() {
        click(logoutButton);

        return new LoginPage(driver)
                .waitUntilLoaded();
    }

    private By expenseRow(String title) {
        return By.xpath(
                "//table//tbody/tr"
                + "[.//td[contains(normalize-space(.), '"
                + title
                + "')]]"
        );
    }

    private By within(WebElement panel, String label) {
        String panelMarker;
        if (panel.findElements(By.xpath(".//h2[normalize-space()='Add expense']")).size() > 0) {
            panelMarker = "Add expense";
        } else if (panel.findElements(By.xpath(".//h2[normalize-space()='Monthly summary']")).size() > 0) {
            panelMarker = "Monthly summary";
        } else {
            panelMarker = "Your expenses";
        }
        return By.xpath("//section[.//h2[normalize-space()='" + panelMarker + "']]//label[contains(normalize-space(.),'" + label + "')]//input");
    }

    private By withinRelative(String label) {
        return By.xpath(".//label[contains(normalize-space(.),'" + label + "')]//input");
    }
}
