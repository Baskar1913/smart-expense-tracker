package com.example.expensetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(5, 600);
    }

    @Test
    void newUsernameShouldBeAllowed() {
        assertTrue(service.isAllowed("baskar"));
    }

    @Test
    void usernameShouldRemainAllowedBeforeFifthFailure() {
        for (int attempt = 1; attempt <= 4; attempt++) {
            service.recordFailure("baskar");
        }

        assertTrue(service.isAllowed("baskar"));
    }

    @Test
    void usernameShouldBeBlockedAfterFiveFailures() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            service.recordFailure("baskar");
        }

        assertFalse(service.isAllowed("baskar"));
    }

    @Test
    void failuresShouldBeTrackedSeparatelyForEachUsername() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            service.recordFailure("first-user");
        }

        assertFalse(service.isAllowed("first-user"));
        assertTrue(service.isAllowed("second-user"));
    }

    @Test
    void usernamesShouldBeCaseInsensitiveAndTrimmed() {
        service.recordFailure(" BASKAR ");
        service.recordFailure("baskar");
        service.recordFailure("Baskar");
        service.recordFailure(" bAsKaR");
        service.recordFailure("BASKAR");

        assertFalse(service.isAllowed("baskar"));
    }

    @Test
    void resetShouldClearOnlySpecifiedUsername() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            service.recordFailure("first-user");
            service.recordFailure("second-user");
        }

        service.reset("first-user");

        assertTrue(service.isAllowed("first-user"));
        assertFalse(service.isAllowed("second-user"));
    }

    @Test
    void expiredAttemptWindowShouldAllowLoginAgain() {
        LoginAttemptService immediatelyExpired = new LoginAttemptService(1, -1);
        immediatelyExpired.recordFailure("baskar");

        assertTrue(immediatelyExpired.isAllowed("baskar"));
    }
}
