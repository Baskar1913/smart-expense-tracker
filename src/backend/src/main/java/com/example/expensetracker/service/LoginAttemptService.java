package com.example.expensetracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    /*
     * Stores failed login attempts separately
     * for each username.
     *
     * Example:
     * baskar -> 5 failures
     * arun   -> 1 failure
     */
    private final ConcurrentHashMap<String, AttemptWindow>
            loginAttempts = new ConcurrentHashMap<>();

    private final int maximumFailures;
    private final long blockingWindowSeconds;

    public LoginAttemptService(
            @Value(
                "${app.security.login-rate-limit.max-failures:5}"
            )
            int maximumFailures,

            @Value(
                "${app.security.login-rate-limit.window-seconds:600}"
            )
            long blockingWindowSeconds
    ) {
        this.maximumFailures = maximumFailures;
        this.blockingWindowSeconds =
                blockingWindowSeconds;
    }

    /*
     * Checks whether the given username is currently
     * allowed to attempt login.
     */
    public boolean isAllowed(String username) {

        String normalizedUsername =
                normalizeUsername(username);

        AttemptWindow attemptWindow =
                loginAttempts.get(normalizedUsername);

        /*
         * No previous failed attempts.
         */
        if (attemptWindow == null) {
            return true;
        }

        Instant currentTime = Instant.now();

        Instant windowExpiry =
                attemptWindow.firstFailureTime()
                        .plusSeconds(
                                blockingWindowSeconds
                        );

        /*
         * The blocking period has expired.
         * Remove the previous attempts and allow login.
         */
        if (currentTime.isAfter(windowExpiry)) {
            loginAttempts.remove(
                    normalizedUsername
            );

            return true;
        }

        /*
         * Allow login while the failure count
         * is below the configured limit.
         */
        return attemptWindow.failureCount()
                < maximumFailures;
    }

    /*
     * Records a failed login attempt for only
     * the given username.
     */
    public void recordFailure(String username) {

        String normalizedUsername =
                normalizeUsername(username);

        Instant currentTime = Instant.now();

        loginAttempts.compute(
                normalizedUsername,
                (key, existingWindow) -> {

                    /*
                     * This is the first failed attempt
                     * for the username.
                     */
                    if (existingWindow == null) {
                        return new AttemptWindow(
                                1,
                                currentTime
                        );
                    }

                    Instant windowExpiry =
                            existingWindow
                                    .firstFailureTime()
                                    .plusSeconds(
                                            blockingWindowSeconds
                                    );

                    /*
                     * The previous failure window expired,
                     * so start a new failure window.
                     */
                    if (currentTime.isAfter(windowExpiry)) {
                        return new AttemptWindow(
                                1,
                                currentTime
                        );
                    }

                    /*
                     * Increment only this username's
                     * failed-attempt count.
                     */
                    return new AttemptWindow(
                            existingWindow
                                    .failureCount() + 1,
                            existingWindow
                                    .firstFailureTime()
                    );
                }
        );
    }

    /*
     * Clears failed attempts for only the
     * specified username.
     *
     * This is called after:
     * - successful login
     * - successful account registration
     */
    public void reset(String username) {

        String normalizedUsername =
                normalizeUsername(username);

        loginAttempts.remove(
                normalizedUsername
        );
    }

    /*
     * Converts usernames into a consistent format.
     *
     * Examples:
     * "Baskar"  -> "baskar"
     * " baskar " -> "baskar"
     */
    private String normalizeUsername(
            String username
    ) {
        if (username == null) {
            return "";
        }

        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /*
     * Stores the failure count and the time at which
     * the current login-attempt window started.
     */
    private record AttemptWindow(
            int failureCount,
            Instant firstFailureTime
    ) {
    }
}