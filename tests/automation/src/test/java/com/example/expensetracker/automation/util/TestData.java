package com.example.expensetracker.automation.util;

import com.example.expensetracker.automation.config.TestConfig;
import com.example.expensetracker.automation.model.TestUser;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public final class TestData {

    private TestData() {
    }

    /*
     * Existing customer account supplied for testing.
     */
    public static TestUser primaryUser() {
        return new TestUser(
                TestConfig.get("test.user1.username"),
                TestConfig.get("test.user1.email"),
                TestConfig.get("test.user1.password")
        );
    }

    /*
     * Second existing customer account supplied for testing.
     */
    public static TestUser secondaryUser() {
        return new TestUser(
                TestConfig.get("test.user2.username"),
                TestConfig.get("test.user2.email"),
                TestConfig.get("test.user2.password")
        );
    }

    /*
     * Creates a unique temporary customer.
     *
     * This is still required for registration, password reset,
     * brute-force and isolated expense test cases.
     */
    public static TestUser newUser(String prefix) {
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toLowerCase(Locale.ROOT);

        String username =
                prefix.toLowerCase(Locale.ROOT)
                        + "_"
                        + suffix;

        return new TestUser(
                username,
                username + "@example.com",
                "Secure@123"
        );
    }

    public static String uniqueTitle(String prefix) {
        return prefix
                + " "
                + UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    public static LocalDate currentDate() {
        return LocalDate.now();
    }
}