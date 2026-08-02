package com.example.expensetracker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpenseApiIntegrationTest extends IntegrationTestSupport {

    private static final String PASSWORD = "Password@123";

    @Test
    void protectedExpenseEndpointShouldRequireAccessToken() throws Exception {
        mockMvc.perform(get("/api/expenses"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message")
                .value("A valid customer access token is required"));
    }

    @Test
    void customerShouldCreateListFilterSearchSummarizeAndDeleteOwnExpenses() throws Exception {
        String username = uniqueUsername("expense");
        register(username, emailFor(username), PASSWORD);
        Tokens tokens = login(username, PASSWORD);

        long lunchId = createExpense(tokens.accessToken(),
            "Lunch", "250.00", " food ", "2026-07-10");
        long taxiId = createExpense(tokens.accessToken(),
            "Airport Taxi", "600.00", "travel", "2026-07-15");
        createExpense(tokens.accessToken(),
            "Internet", "999.00", "utilities", "2026-08-01");

        mockMvc.perform(get("/api/expenses")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/expenses")
                .param("category", "FOOD")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(lunchId))
            .andExpect(jsonPath("$[0].category").value("Food"));

        mockMvc.perform(get("/api/expenses/search")
                .param("query", "taxi")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(taxiId));

        mockMvc.perform(get("/api/expenses/search")
                .param("from", "2026-07-01")
                .param("to", "2026-07-31")
                .param("minAmount", "300")
                .param("maxAmount", "700")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Airport Taxi"));

        mockMvc.perform(get("/api/expenses/total")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1849.00));

        mockMvc.perform(get("/api/expenses/total")
                .param("category", "food")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(250.00));

        mockMvc.perform(get("/api/expenses/total/by-category")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].category").value("Food"))
            .andExpect(jsonPath("$[0].total").value(250.00))
            .andExpect(jsonPath("$[1].category").value("Travel"))
            .andExpect(jsonPath("$[2].category").value("Utilities"));

        mockMvc.perform(get("/api/expenses/summary/monthly")
                .param("year", "2026")
                .param("month", "7")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.year").value(2026))
            .andExpect(jsonPath("$.month").value(7))
            .andExpect(jsonPath("$.expenseCount").value(2))
            .andExpect(jsonPath("$.total").value(850.00))
            .andExpect(jsonPath("$.totalsByCategory.Food").value(250.00))
            .andExpect(jsonPath("$.totalsByCategory.Travel").value(600.00));

        mockMvc.perform(delete("/api/expenses/{id}", taxiId)
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Expense deleted successfully"));

        mockMvc.perform(get("/api/expenses")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void customerShouldNeverSeeOrDeleteAnotherCustomersExpense() throws Exception {
        String firstUsername = uniqueUsername("owner");
        String secondUsername = uniqueUsername("other");
        register(firstUsername, emailFor(firstUsername), PASSWORD);
        register(secondUsername, emailFor(secondUsername), PASSWORD);

        Tokens firstTokens = login(firstUsername, PASSWORD);
        Tokens secondTokens = login(secondUsername, PASSWORD);

        long firstExpenseId = createExpense(
            firstTokens.accessToken(), "Private Expense", "500.00", "Personal", "2026-07-20"
        );
        createExpense(
            secondTokens.accessToken(), "Other Expense", "900.00", "Travel", "2026-07-21"
        );

        mockMvc.perform(get("/api/expenses")
                .header("Authorization", bearer(firstTokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Private Expense"));

        mockMvc.perform(delete("/api/expenses/{id}", firstExpenseId)
                .header("Authorization", bearer(secondTokens.accessToken())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Expense not found: " + firstExpenseId));

        mockMvc.perform(get("/api/expenses")
                .header("Authorization", bearer(firstTokens.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void expenseEndpointsShouldRejectInvalidInputAndRanges() throws Exception {
        String username = uniqueUsername("invalidexpense");
        register(username, emailFor(username), PASSWORD);
        Tokens tokens = login(username, PASSWORD);

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", bearer(tokens.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", "",
                    "amount", BigDecimal.ZERO,
                    "category", "",
                    "date", "2026-07-10"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors.title").exists())
            .andExpect(jsonPath("$.validationErrors.amount").exists())
            .andExpect(jsonPath("$.validationErrors.category").exists());

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", bearer(tokens.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", "Invalid year",
                    "amount", new BigDecimal("10.00"),
                    "category", "Other",
                    "date", "202666-06-12"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Enter a valid date using the yyyy-MM-dd format and a four-digit year"));

        mockMvc.perform(get("/api/expenses/search")
                .param("from", "202666-06-12")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid value for parameter: from"));

        mockMvc.perform(get("/api/expenses/search")
                .param("from", "2026-08-01")
                .param("to", "2026-07-01")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("'from' date cannot be after 'to' date"));

        mockMvc.perform(get("/api/expenses/search")
                .param("minAmount", "1000")
                .param("maxAmount", "100")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("minAmount cannot be greater than maxAmount"));

        mockMvc.perform(get("/api/expenses/summary/monthly")
                .param("year", "2026")
                .param("month", "13")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid year or month"));
    }

    @Test
    void refreshTokenMustNotBeAcceptedAsAnAccessToken() throws Exception {
        String username = uniqueUsername("tokentype");
        register(username, emailFor(username), PASSWORD);
        Tokens tokens = login(username, PASSWORD);

        mockMvc.perform(get("/api/expenses")
                .header("Authorization", bearer(tokens.refreshToken())))
            .andExpect(status().isUnauthorized());
    }

    private long createExpense(
        String accessToken,
        String title,
        String amount,
        String category,
        String date
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/expenses")
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", title,
                    "amount", new BigDecimal(amount),
                    "category", category,
                    "date", date
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.amount").value(new BigDecimal(amount).doubleValue()))
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }
}
