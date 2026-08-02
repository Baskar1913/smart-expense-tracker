package com.example.expensetracker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiIntegrationTest extends IntegrationTestSupport {

    private static final String PASSWORD = "Password@123";

    @Test
    void registerShouldCreateCustomerAccount() throws Exception {
        String username = uniqueUsername("register");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "email", emailFor(username),
                    "password", PASSWORD
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message")
                .value("Account created successfully. Please sign in."));
    }

    @Test
    void registerShouldRejectDuplicateUsernameAndEmail() throws Exception {
        String username = uniqueUsername("duplicate");
        String email = emailFor(username);
        register(username, email, PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username.toUpperCase(),
                    "email", "another@example.com",
                    "password", PASSWORD
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", uniqueUsername("other"),
                    "email", email.toUpperCase(),
                    "password", PASSWORD
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void registerShouldValidateInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", "a",
                    "email", "not-an-email",
                    "password", "short"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors.username").exists())
            .andExpect(jsonPath("$.validationErrors.email").exists())
            .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void loginShouldReturnJwtTokensForCorrectCredentials() throws Exception {
        String username = uniqueUsername("login");
        register(username, emailFor(username), PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username.toUpperCase(),
                    "password", PASSWORD
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void loginShouldRejectWrongPassword() throws Exception {
        String username = uniqueUsername("wrongpass");
        register(username, emailFor(username), PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "password", "WrongPassword@123"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void failedAttemptsForOneUsernameShouldNotBlockAnotherUsername() throws Exception {
        String blockedUser = uniqueUsername("blocked");
        String normalUser = uniqueUsername("normal");
        register(blockedUser, emailFor(blockedUser), PASSWORD);
        register(normalUser, emailFor(normalUser), PASSWORD);

        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of(
                        "username", blockedUser,
                        "password", "WrongPassword@123"
                    ))))
                .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", blockedUser,
                    "password", PASSWORD
                ))))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message", containsString("this username")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", normalUser,
                    "password", PASSWORD
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(normalUser));
    }

    @Test
    void forgotPasswordShouldCheckVerifyAndResetPassword() throws Exception {
        String username = uniqueUsername("forgot");
        String email = emailFor(username);
        String newPassword = "NewPassword@456";
        register(username, email, PASSWORD);

        mockMvc.perform(post("/api/auth/forgot-password/check-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", username))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true));

        mockMvc.perform(post("/api/auth/forgot-password/check-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", uniqueUsername("missing")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
            .andExpect(jsonPath("$.message", containsString("Create an account")));

        mockMvc.perform(post("/api/auth/forgot-password/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "email", "wrong@example.com"
                ))))
            .andExpect(status().isUnauthorized());

        MvcResult verifyResult = mockMvc.perform(post("/api/auth/forgot-password/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "email", email
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resetToken").isNotEmpty())
            .andReturn();

        String resetToken = objectMapper
            .readTree(verifyResult.getResponse().getContentAsString())
            .get("resetToken")
            .asText();

        mockMvc.perform(post("/api/auth/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "resetToken", resetToken,
                    "newPassword", newPassword
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", containsString("Password reset successfully")));

        mockMvc.perform(post("/api/auth/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "resetToken", resetToken,
                    "newPassword", "AnotherPassword@789"
                ))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "password", PASSWORD
                ))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "username", username,
                    "password", newPassword
                ))))
            .andExpect(status().isOk());
    }

    @Test
    void refreshShouldRotateRefreshTokenAndRejectItsReuse() throws Exception {
        String username = uniqueUsername("refresh");
        register(username, emailFor(username), PASSWORD);
        Tokens original = login(username, PASSWORD);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", original.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andReturn();

        JsonNode refreshed = objectMapper.readTree(
            refreshResult.getResponse().getContentAsString()
        );

        org.junit.jupiter.api.Assertions.assertNotEquals(
            original.refreshToken(),
            refreshed.get("refreshToken").asText()
        );

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", original.refreshToken()))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutShouldRevokeAccessAndRefreshTokens() throws Exception {
        String username = uniqueUsername("logout");
        register(username, emailFor(username), PASSWORD);
        Tokens tokens = login(username, PASSWORD);

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", bearer(tokens.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", tokens.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/expenses")
                .header("Authorization", bearer(tokens.accessToken())))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", tokens.refreshToken()))))
            .andExpect(status().isUnauthorized());
    }
}
