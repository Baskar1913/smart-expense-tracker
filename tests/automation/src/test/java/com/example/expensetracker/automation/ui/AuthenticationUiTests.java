package com.example.expensetracker.automation.ui;

import com.example.expensetracker.automation.model.TestUser;
import com.example.expensetracker.automation.ui.pages.DashboardPage;
import com.example.expensetracker.automation.ui.pages.LoginPage;
import com.example.expensetracker.automation.util.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
@DisplayName("Authentication UI")
class AuthenticationUiTests extends BaseUiTest {

    @Test
    @DisplayName(
            "UI-AUTH-01 Login fields start blank and development credentials are hidden"
    )
    void loginFieldsAreBlank() {

        LoginPage page = new LoginPage(driver).open();

        assertEquals("", page.loginUsernameValue());
        assertEquals("", page.loginPasswordValue());
        assertFalse(page.containsDevelopmentAccountsText());
    }

    @Test
    @DisplayName(
            "UI-AUTH-02 Customer can create an account and sign in"
    )
    void customerCanRegisterAndLogin() {

        TestUser user = TestData.newUser("ui_register");

        LoginPage page = new LoginPage(driver)
                .open()
                .openRegistration();

        page.register(user, user.password());

        assertTrue(
                page.successMessage()
                        .contains("Account created successfully")
        );

        DashboardPage dashboard =
                page.loginSuccessfully(
                        user.username(),
                        user.password()
                );

        assertEquals(
                user.username(),
                dashboard.displayedUsername()
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-03 Registration rejects mismatched confirmation password"
    )
    void registrationRejectsMismatchedPasswords() {

        TestUser user = TestData.newUser("ui_mismatch");

        LoginPage page = new LoginPage(driver)
                .open()
                .openRegistration();

        page.register(user, "Different@123");

        assertEquals(
                "Passwords do not match",
                page.errorMessage()
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-04 Invalid login displays an error"
    )
    void invalidLoginDisplaysError() {

        TestUser user = TestData.newUser("ui_invalid");

        LoginPage page = new LoginPage(driver).open();

        page.submitLogin(
                user.username(),
                "WrongPassword@123"
        );

        page.waitForErrorContaining(
                "Invalid username or password"
        );

        assertTrue(
                page.errorMessage()
                        .contains("Invalid username or password")
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-05 Unknown forgot-password username offers account creation"
    )
    void unknownForgotPasswordOffersCreateAccount() {

        TestUser user = TestData.newUser("ui_missing");

        LoginPage page = new LoginPage(driver)
                .open()
                .openForgotPassword(user.username());

        page.checkForgotUsername();

        page.waitForErrorContaining("does not exist");

        String errorMessage = page.errorMessage()
                .toLowerCase();

        assertTrue(
                errorMessage.contains("does not exist"),
                "Expected account-not-found error, but received: "
                        + errorMessage
        );

        assertTrue(
                page.createAccountOptionIsDisplayed(),
                "Create account option should be displayed"
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-06 Existing customer can reset password and login"
    )
    void existingCustomerCanResetPasswordAndLogin() {

        TestUser user = TestData.newUser("ui_reset");

        api.register(user)
                .then()
                .statusCode(201);

        LoginPage page = new LoginPage(driver)
                .open()
                .openForgotPassword(user.username());

        page.checkForgotUsername();
        page.verifyRegisteredEmail(user.email());

        String changedPassword = "Changed@123";

        page.resetPassword(changedPassword);

        assertTrue(
                page.successMessage()
                        .contains("Password reset")
        );

        DashboardPage dashboard =
                page.loginSuccessfully(
                        user.username(),
                        changedPassword
                );

        assertEquals(
                user.username(),
                dashboard.displayedUsername()
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-07 Blocking one username does not block a second username"
    )
    void blockingOneUsernameDoesNotBlockSecondUsername() {

        TestUser blocked =
                TestData.newUser("ui_blocked");

        TestUser allowed =
                TestData.newUser("ui_allowed");

        api.register(blocked)
                .then()
                .statusCode(201);

        api.register(allowed)
                .then()
                .statusCode(201);

        LoginPage page = new LoginPage(driver).open();

        for (int attempt = 1; attempt <= 5; attempt++) {

            page.submitLogin(
                    blocked.username(),
                    "WrongPassword@123"
            );

            page.waitForErrorContaining(
                    "Invalid username or password"
            );
        }

        page.submitLogin(
                blocked.username(),
                "WrongPassword@123"
        );

        page.waitForErrorContaining(
                "Too many failed login attempts"
        );

        DashboardPage dashboard =
                page.loginSuccessfully(
                        allowed.username(),
                        allowed.password()
                );

        assertEquals(
                allowed.username(),
                dashboard.displayedUsername()
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-08 Baskar19 can login through the UI"
    )
    void primaryExistingCustomerCanLogin() {

        TestUser user = TestData.primaryUser();

        LoginPage loginPage =
                new LoginPage(driver).open();

        DashboardPage dashboard =
                loginPage.loginSuccessfully(
                        user.username(),
                        user.password()
                );

        assertTrue(
                user.username().equalsIgnoreCase(
                        dashboard.displayedUsername()
                ),
                "Expected username "
                        + user.username()
                        + " but displayed "
                        + dashboard.displayedUsername()
        );
    }

    @Test
    @DisplayName(
            "UI-AUTH-09 Baskar can login through the UI"
    )
    void secondaryExistingCustomerCanLogin() {

        TestUser user = TestData.secondaryUser();

        LoginPage loginPage =
                new LoginPage(driver).open();

        DashboardPage dashboard =
                loginPage.loginSuccessfully(
                        user.username(),
                        user.password()
                );

        assertTrue(
            user.username().equalsIgnoreCase(
                    dashboard.displayedUsername()
            ),
            "Expected username "
                    + user.username()
                    + " but displayed "
                    + dashboard.displayedUsername()
        );
    }
}