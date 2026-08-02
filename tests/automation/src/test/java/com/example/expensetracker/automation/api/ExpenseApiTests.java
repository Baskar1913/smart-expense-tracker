package com.example.expensetracker.automation.api;

import com.example.expensetracker.automation.model.TestUser;
import com.example.expensetracker.automation.model.TokenPair;
import com.example.expensetracker.automation.util.TestData;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
@DisplayName("Expense API")
class ExpenseApiTests {

    private ExpenseTrackerApiClient api;
    private TestUser user;
    private TokenPair tokens;

    @BeforeEach
    void setUp() {
        api = new ExpenseTrackerApiClient();
        user = TestData.newUser("expense");
        tokens = api.registerAndLogin(user);
    }

    @Test
    @DisplayName("API-EXP-01 Protected expense endpoint requires authentication")
    void protectedEndpointRequiresAuthentication() {
        api.getExpensesWithoutToken()
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("API-EXP-02 Create an expense")
    void createExpense() {
        api.createExpense(
                        tokens.accessToken(),
                        "Lunch",
                        new BigDecimal("250.50"),
                        "food",
                        LocalDate.of(2026, 7, 31)
                )
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Lunch"))
                .body("category", equalTo("Food"))
                .body("date", equalTo("2026-07-31"));
    }

    @Test
    @DisplayName("API-EXP-03 List only the authenticated customer's expenses")
    void listOnlyOwnExpenses() {
        api.createExpense(tokens.accessToken(), "Own expense", new BigDecimal("100"),
                "Personal", LocalDate.now()).then().statusCode(201);

        TestUser otherUser = TestData.newUser("other");
        TokenPair otherTokens = api.registerAndLogin(otherUser);

        api.listExpenses(tokens.accessToken())
                .then()
                .statusCode(200)
                .body("title", hasItem("Own expense"));

        api.listExpenses(otherTokens.accessToken())
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("API-EXP-04 Filter expenses by category")
    void filterExpensesByCategory() {
        api.createExpense(tokens.accessToken(), "Dinner", new BigDecimal("500"),
                "Food", LocalDate.now()).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Bus", new BigDecimal("80"),
                "Travel", LocalDate.now()).then().statusCode(201);

        api.listExpensesByCategory(tokens.accessToken(), "Food")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].title", equalTo("Dinner"))
                .body("[0].category", equalTo("Food"));
    }

    @Test
    @DisplayName("API-EXP-05 Search by title, date and amount range")
    void searchExpenses() {
        api.createExpense(tokens.accessToken(), "Office lunch", new BigDecimal("220"),
                "Food", LocalDate.of(2026, 7, 10)).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Weekend movie", new BigDecimal("450"),
                "Entertainment", LocalDate.of(2026, 7, 12)).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Train ticket", new BigDecimal("900"),
                "Travel", LocalDate.of(2026, 8, 1)).then().statusCode(201);

        api.searchExpenses(
                        tokens.accessToken(),
                        "lunch",
                        "Food",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31),
                        new BigDecimal("200"),
                        new BigDecimal("300")
                )
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].title", equalTo("Office lunch"));
    }

    @Test
    @DisplayName("API-EXP-06 Calculate overall and category totals")
    void calculateTotals() {
        api.createExpense(tokens.accessToken(), "Breakfast", new BigDecimal("100"),
                "Food", LocalDate.now()).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Dinner", new BigDecimal("250"),
                "Food", LocalDate.now()).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Taxi", new BigDecimal("400"),
                "Travel", LocalDate.now()).then().statusCode(201);

        assertMoney(api.total(tokens.accessToken(), null), "750");
        assertMoney(api.total(tokens.accessToken(), "Food"), "350");
    }

    @Test
    @DisplayName("API-EXP-07 Calculate totals grouped by category")
    void totalsGroupedByCategory() {
        api.createExpense(tokens.accessToken(), "Snack", new BigDecimal("50"),
                "Food", LocalDate.now()).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "Metro", new BigDecimal("60"),
                "Travel", LocalDate.now()).then().statusCode(201);

        api.totalsByCategory(tokens.accessToken())
                .then()
                .statusCode(200)
                .body("category", hasItems("Food", "Travel"))
                .body("total", hasItems(50.0f, 60.0f));
    }

    @Test
    @DisplayName("API-EXP-08 Return the selected monthly summary")
    void monthlySummary() {
        api.createExpense(tokens.accessToken(), "July food", new BigDecimal("300"),
                "Food", LocalDate.of(2026, 7, 5)).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "July travel", new BigDecimal("700"),
                "Travel", LocalDate.of(2026, 7, 20)).then().statusCode(201);
        api.createExpense(tokens.accessToken(), "August expense", new BigDecimal("900"),
                "Travel", LocalDate.of(2026, 8, 1)).then().statusCode(201);

        Response response = api.monthlySummary(tokens.accessToken(), 2026, 7);
        response.then()
                .statusCode(200)
                .body("year", equalTo(2026))
                .body("month", equalTo(7))
                .body("expenseCount", equalTo(2));

        BigDecimal total = new BigDecimal(response.jsonPath().get("total").toString());
        assertEquals(0, total.compareTo(new BigDecimal("1000")));
    }

    @Test
    @DisplayName("API-EXP-09 Delete an owned expense")
    void deleteOwnedExpense() {
        long expenseId = api.createExpense(tokens.accessToken(), "Delete me", new BigDecimal("75"),
                        "Other", LocalDate.now())
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        api.deleteExpense(tokens.accessToken(), expenseId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Expense deleted successfully"));

        Response listed = api.listExpenses(tokens.accessToken());
        listed.then().statusCode(200);
        boolean deletedIdStillPresent = listed.jsonPath().getList("id", Long.class)
                .stream()
                .anyMatch(id -> id == expenseId);
        assertEquals(false, deletedIdStillPresent);
    }

    @Test
    @DisplayName("API-EXP-10 Prevent deleting another customer's expense")
    void preventDeletingAnotherCustomersExpense() {
        long expenseId = api.createExpense(tokens.accessToken(), "Private expense", new BigDecimal("125"),
                        "Other", LocalDate.now())
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        TestUser otherUser = TestData.newUser("intruder");
        TokenPair otherTokens = api.registerAndLogin(otherUser);

        api.deleteExpense(otherTokens.accessToken(), expenseId)
                .then()
                .statusCode(404)
                .body("message", containsString("Expense not found"));
    }

    @Test
    @DisplayName("API-EXP-11 Reject invalid expense input")
    void rejectInvalidExpense() {
        api.createExpense(tokens.accessToken(), "Invalid", BigDecimal.ZERO,
                        "Food", LocalDate.now())
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    @DisplayName("API-EXP-12 Reject invalid search ranges")
    void rejectInvalidSearchRange() {
        api.searchExpenses(
                        tokens.accessToken(),
                        null,
                        null,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 7, 1),
                        null,
                        null
                )
                .then()
                .statusCode(400)
                .body("message", containsString("cannot be after"));
    }

    @Test
    @DisplayName("API-EXP-13 Reject an invalid month")
    void rejectInvalidMonth() {
        api.monthlySummary(tokens.accessToken(), 2026, 13)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid year or month"));
    }

    private void assertMoney(Response response, String expected) {
        response.then().statusCode(200);
        BigDecimal actual = new BigDecimal(response.jsonPath().get("total").toString());
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}
