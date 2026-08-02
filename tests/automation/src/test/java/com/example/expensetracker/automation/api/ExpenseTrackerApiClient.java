package com.example.expensetracker.automation.api;

import com.example.expensetracker.automation.config.TestConfig;
import com.example.expensetracker.automation.model.TestUser;
import com.example.expensetracker.automation.model.TokenPair;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseTrackerApiClient {

    private final RequestSpecification request;

    public ExpenseTrackerApiClient() {
        request = RestAssured.given()
                .baseUri(TestConfig.apiBaseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public Response register(TestUser user) {
        return request.body(Map.of(
                        "username", user.username(),
                        "email", user.email(),
                        "password", user.password()
                ))
                .post("/auth/register");
    }

    public Response register(String username, String email, String password) {
        return request.body(Map.of(
                        "username", username,
                        "email", email,
                        "password", password
                ))
                .post("/auth/register");
    }

    public Response login(String username, String password) {
        return request.body(Map.of(
                        "username", username,
                        "password", password
                ))
                .post("/auth/login");
    }

    public TokenPair registerAndLogin(TestUser user) {
        register(user).then().statusCode(201);
        Response response = login(user.username(), user.password());
        response.then().statusCode(200);
        return new TokenPair(
                response.jsonPath().getString("accessToken"),
                response.jsonPath().getString("refreshToken"),
                response.jsonPath().getString("username")
        );
    }

    public Response checkForgotPasswordUser(String username) {
        return request.body(Map.of("username", username))
                .post("/auth/forgot-password/check-user");
    }

    public Response verifyForgotPassword(String username, String email) {
        return request.body(Map.of("username", username, "email", email))
                .post("/auth/forgot-password/verify");
    }

    public Response resetPassword(String resetToken, String newPassword) {
        return request.body(Map.of(
                        "resetToken", resetToken,
                        "newPassword", newPassword
                ))
                .post("/auth/forgot-password/reset");
    }

    public Response refresh(String refreshToken) {
        return request.body(Map.of("refreshToken", refreshToken))
                .post("/auth/refresh");
    }

    public Response logout(String accessToken, String refreshToken) {
        return authorized(accessToken)
                .body(Map.of("refreshToken", refreshToken))
                .post("/auth/logout");
    }

    public Response createExpense(
            String accessToken,
            String title,
            BigDecimal amount,
            String category,
            LocalDate date
    ) {
        return authorized(accessToken)
                .body(Map.of(
                        "title", title,
                        "amount", amount,
                        "category", category,
                        "date", date.toString()
                ))
                .post("/expenses");
    }

    public Response listExpenses(String accessToken) {
        return authorized(accessToken).get("/expenses");
    }

    public Response listExpensesByCategory(String accessToken, String category) {
        return authorized(accessToken)
                .queryParam("category", category)
                .get("/expenses");
    }

    public Response searchExpenses(
            String accessToken,
            String query,
            String category,
            LocalDate from,
            LocalDate to,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
    	Map<String, Object> parameters =
    	        new LinkedHashMap<>();

    	putIfPresent(
    	        parameters,
    	        "query",
    	        query
    	);

    	putIfPresent(
    	        parameters,
    	        "category",
    	        category
    	);

    	putIfPresent(
    	        parameters,
    	        "from",
    	        from == null ? null : from.toString()
    	);

    	putIfPresent(
    	        parameters,
    	        "to",
    	        to == null ? null : to.toString()
    	);

    	putIfPresent(
    	        parameters,
    	        "minAmount",
    	        minAmount
    	);

    	putIfPresent(
    	        parameters,
    	        "maxAmount",
    	        maxAmount
    	);

    	return authorized(accessToken)
    	        .queryParams(parameters)
    	        .get("/expenses/search");
    }

    public Response total(String accessToken, String category) {
        RequestSpecification specification = authorized(accessToken);
        if (category != null && !category.isBlank()) {
            specification.queryParam("category", category);
        }
        return specification.get("/expenses/total");
    }

    public Response totalsByCategory(String accessToken) {
        return authorized(accessToken).get("/expenses/total/by-category");
    }

    public Response monthlySummary(String accessToken, int year, int month) {
        return authorized(accessToken)
                .queryParam("year", year)
                .queryParam("month", month)
                .get("/expenses/summary/monthly");
    }

    public Response deleteExpense(String accessToken, long expenseId) {
        return authorized(accessToken).delete("/expenses/{id}", expenseId);
    }

    public Response getExpensesWithoutToken() {
        return request.get("/expenses");
    }

    private RequestSpecification authorized(String accessToken) {
        return RestAssured.given()
                .baseUri(TestConfig.apiBaseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken);
    }

    private void putIfPresent(Map<String, Object> parameters, String key, Object value) {
        if (value != null && (!(value instanceof String text) || !text.isBlank())) {
            parameters.put(key, value);
        }
    }
}
