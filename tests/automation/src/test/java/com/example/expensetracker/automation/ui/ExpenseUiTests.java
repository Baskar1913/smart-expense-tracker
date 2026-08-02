package com.example.expensetracker.automation.ui;

import com.example.expensetracker.automation.model.TestUser;
import com.example.expensetracker.automation.model.TokenPair;
import com.example.expensetracker.automation.ui.pages.DashboardPage;
import com.example.expensetracker.automation.ui.pages.LoginPage;
import com.example.expensetracker.automation.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
@DisplayName("Expense UI")
class ExpenseUiTests extends BaseUiTest {

    private TestUser user;
    private TokenPair tokens;
    private DashboardPage dashboard;

    @BeforeEach
    void createUserAndLogin() {

        user = TestData.newUser("ui_expense");

        tokens = api.registerAndLogin(user);

        dashboard = new LoginPage(driver)
                .open()
                .loginSuccessfully(
                        user.username(),
                        user.password()
                );
    }

    @Test
    @DisplayName(
            "UI-EXP-01 Category starts blank and new expense appears without an ID column"
    )
    void addExpenseWithoutShowingId() {

        assertEquals(
                "",
                dashboard.addExpenseCategoryValue()
        );

        assertFalse(
                dashboard.hasTableHeader("ID")
        );

        String title =
                TestData.uniqueTitle("Automation lunch");

        dashboard.addExpense(
                title,
                new BigDecimal("250.50"),
                "Food",
                LocalDate.now()
        );

        dashboard.waitForExpense(title);

        assertTrue(
                dashboard.expenseIsVisible(title)
        );

        assertFalse(
                dashboard.hasTableHeader("ID")
        );
    }

    @Test
    @DisplayName(
            "UI-EXP-02 Search and category filtering show the matching expense"
    )
    void searchAndFilterExpenses() {

        String foodTitle =
                TestData.uniqueTitle("Office lunch");

        String travelTitle =
                TestData.uniqueTitle("Train ticket");

        api.createExpense(
                tokens.accessToken(),
                foodTitle,
                new BigDecimal("225"),
                "Food",
                LocalDate.of(2026, 7, 10)
        )
        .then()
        .statusCode(201);

        api.createExpense(
                tokens.accessToken(),
                travelTitle,
                new BigDecimal("900"),
                "Travel",
                LocalDate.of(2026, 7, 12)
        )
        .then()
        .statusCode(201);

        driver.navigate().refresh();

        dashboard.waitUntilLoaded();

        dashboard.search(
                "lunch",
                "Food",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        dashboard.waitForExpense(foodTitle);

        /*
         * Wait for React to finish filtering the table.
         * The Travel expense should disappear.
         */
        dashboard.waitForExpenseToDisappear(
                travelTitle
        );

        assertTrue(
                dashboard.expenseIsVisible(foodTitle)
        );

        assertFalse(
                dashboard.expenseIsVisible(travelTitle)
        );
    }

    @Test
    @DisplayName(
            "UI-EXP-03 Total spend card is updated"
    )
    void totalSpendIsUpdated() {

        api.createExpense(
                tokens.accessToken(),
                "Breakfast",
                new BigDecimal("100"),
                "Food",
                LocalDate.now()
        )
        .then()
        .statusCode(201);

        api.createExpense(
                tokens.accessToken(),
                "Taxi",
                new BigDecimal("400"),
                "Travel",
                LocalDate.now()
        )
        .then()
        .statusCode(201);

        driver.navigate().refresh();

        dashboard.waitUntilLoaded();

        String total =
                dashboard.totalSpendText();

        assertTrue(
                total.contains("500"),
                "Expected total spend to contain 500, but was: "
                        + total
        );
    }

    @Test
    @DisplayName(
            "UI-EXP-04 Monthly summary downloads the selected month as a text file"
    )
    void monthlySummaryDownloadsTextFile()
            throws IOException {

        api.createExpense(
                tokens.accessToken(),
                "July meal",
                new BigDecimal("300"),
                "Food",
                LocalDate.of(2026, 7, 5)
        )
        .then()
        .statusCode(201);

        api.createExpense(
                tokens.accessToken(),
                "July taxi",
                new BigDecimal("700"),
                "Travel",
                LocalDate.of(2026, 7, 20)
        )
        .then()
        .statusCode(201);

        Path downloaded =
                dashboard.loadAndDownloadMonthlySummary(
                        2026,
                        7
                );

        String content =
                Files.readString(downloaded);

        assertTrue(
                content.contains("Year: 2026")
        );

        assertTrue(
                content.contains("Month: July (07)")
        );

        assertTrue(
                content.contains("Number of expenses: 2")
        );

        assertTrue(
                content.contains("Food")
        );

        assertTrue(
                content.contains("Travel")
        );
    }

    @Test
    @DisplayName(
            "UI-EXP-05 Customer can delete an owned expense"
    )
    void deleteExpense() {

        String title =
                TestData.uniqueTitle(
                        "Delete this expense"
                );

        dashboard.addExpense(
                title,
                new BigDecimal("80"),
                "Other",
                LocalDate.now()
        );

        dashboard.waitForExpense(title);

        dashboard.deleteExpense(title);

        dashboard.waitForExpenseToDisappear(
                title
        );

        assertFalse(
                dashboard.expenseIsVisible(title)
        );
    }

    @Test
    @DisplayName(
            "UI-EXP-06 Logout returns to the login screen"
    )
    void logoutReturnsToLogin() {

        LoginPage loginPage =
                dashboard.logout();

        loginPage.waitUntilLoaded();

        assertTrue(
                loginPage.isDisplayed()
        );
    }
}