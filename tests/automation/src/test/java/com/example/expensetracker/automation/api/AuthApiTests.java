package com.example.expensetracker.automation.api;

import com.example.expensetracker.automation.model.TestUser;
import com.example.expensetracker.automation.model.TokenPair;
import com.example.expensetracker.automation.util.TestData;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("api")
@DisplayName("Authentication API")
class AuthApiTests {

    private ExpenseTrackerApiClient api;

    @BeforeEach
    void setUp() {
        api = new ExpenseTrackerApiClient();
    }

    @Test
    @DisplayName("API-AUTH-01 Register a new customer")
    void registerNewCustomer() {
        TestUser user = TestData.newUser("register");

        api.register(user)
                .then()
                .statusCode(201)
                .body("message", containsString("Account created successfully"));
    }

    @Test
    @DisplayName("API-AUTH-02 Reject duplicate username")
    void rejectDuplicateUsername() {
        TestUser user = TestData.newUser("duplicate");
        api.register(user).then().statusCode(201);

        api.register(user.username(), "another_" + user.email(), user.password())
                .then()
                .statusCode(400)
                .body("message", equalTo("Username already exists"));
    }

    @Test
    @DisplayName("API-AUTH-03 Validate registration input")
    void validateRegistrationInput() {
        api.register("x", "not-an-email", "short")
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("validationErrors", not(anEmptyMap()));
    }

    @Test
    @Disabled()
    @DisplayName(
            "API-AUTH-04 can login and receive tokens"
    )
    void loginReturnsTokens() {

        TestUser user = TestData.primaryUser();

        api.login(
                user.username(),
                user.password()
        )
        .then()
        .statusCode(200)
        .body(
                "accessToken",
                not(blankOrNullString())
        )
        .body(
                "refreshToken",
                not(blankOrNullString())
        )
        .body(
                "tokenType",
                equalTo("Bearer")
        )
        .body(
                "username",
                equalTo("Baskar19")
        );
    }

    @Test
    @DisplayName("API-AUTH-05 Reject invalid password")
    void rejectInvalidPassword() {
        TestUser user = TestData.newUser("invalid_login");
        api.register(user).then().statusCode(201);

        api.login(user.username(), "WrongPassword@123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Invalid username or password"));
    }

    @Test
    @DisplayName("API-AUTH-06 Failed attempts for one username do not block another username")
    void failedAttemptsForOneUsernameDoNotBlockAnotherUsername() {
        TestUser blockedUser = TestData.newUser("blocked");
        TestUser allowedUser = TestData.newUser("allowed");
        api.register(blockedUser).then().statusCode(201);
        api.register(allowedUser).then().statusCode(201);

        for (int attempt = 1; attempt <= 5; attempt++) {
            api.login(blockedUser.username(), "WrongPassword@123")
                    .then()
                    .statusCode(401);
        }

        api.login(blockedUser.username(), "WrongPassword@123")
                .then()
                .statusCode(429)
                .body("message", containsString("Too many failed login attempts"));

        api.login(allowedUser.username(), allowedUser.password())
                .then()
                .statusCode(200)
                .body("username", equalTo(allowedUser.username()));
    }

    @Test
    @DisplayName("API-AUTH-07 Forgot-password check identifies an unknown username")
    void forgotPasswordCheckUnknownUser() {
        String username = TestData.newUser("missing").username();

        api.checkForgotPasswordUser(username)
                .then()
                .statusCode(200)
                .body("exists", equalTo(false))
                .body("message", containsString("does not exist"));
    }

    @Test
    @DisplayName("API-AUTH-08 Verify account and reset password")
    void verifyAndResetPassword() {
        TestUser user = TestData.newUser("reset");
        api.register(user).then().statusCode(201);

        api.checkForgotPasswordUser(user.username())
                .then()
                .statusCode(200)
                .body("exists", equalTo(true));

        String resetToken = api.verifyForgotPassword(user.username(), user.email())
                .then()
                .statusCode(200)
                .body("resetToken", not(blankOrNullString()))
                .extract()
                .path("resetToken");

        String newPassword = "Changed@123";
        api.resetPassword(resetToken, newPassword)
                .then()
                .statusCode(200)
                .body("message", containsString("Password reset"));

        api.login(user.username(), user.password()).then().statusCode(401);
        api.login(user.username(), newPassword).then().statusCode(200);
    }

    @Test
    @DisplayName("API-AUTH-09 Refresh token rotation rejects reuse of old refresh token")
    void refreshTokenRotation() {
        TestUser user = TestData.newUser("refresh");
        TokenPair firstPair = api.registerAndLogin(user);

        Response refreshed = api.refresh(firstPair.refreshToken());
        refreshed.then().statusCode(200);
        String secondRefreshToken = refreshed.jsonPath().getString("refreshToken");
        assertNotEquals(firstPair.refreshToken(), secondRefreshToken);

        api.refresh(firstPair.refreshToken())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("API-AUTH-10 Logout revokes the access token")
    void logoutRevokesAccessToken() {
        TestUser user = TestData.newUser("logout");
        TokenPair tokens = api.registerAndLogin(user);

        api.logout(tokens.accessToken(), tokens.refreshToken())
                .then()
                .statusCode(200)
                .body("message", equalTo("Logged out successfully"));

        api.listExpenses(tokens.accessToken())
                .then()
                .statusCode(401);
    }
    
    @Test
    @Disabled()
    @DisplayName(
            "API-AUTH-11 can login successfully"
    )
    void secondExistingCustomerCanLogin() {

        TestUser user = TestData.secondaryUser();

        api.login(
                user.username(),
                user.password()
        )
        .then()
        .statusCode(200)
        .body(
                "accessToken",
                not(blankOrNullString())
        )
        .body(
                "refreshToken",
                not(blankOrNullString())
        )
        .body(
                "username",
                equalTo("Baskar")
        );
    }
}
