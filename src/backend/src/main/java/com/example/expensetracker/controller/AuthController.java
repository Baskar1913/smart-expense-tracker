package com.example.expensetracker.controller;

import com.example.expensetracker.dto.auth.*;
import com.example.expensetracker.exception.TooManyRequestsException;
import com.example.expensetracker.exception.UnauthorizedException;
import com.example.expensetracker.security.JwtAuthenticationFilter;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.LoginAttemptService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            AuthService authService,
            LoginAttemptService loginAttemptService
    ) {
        this.authService = authService;
        this.loginAttemptService = loginAttemptService;
    }

    /*
     * Create a new customer account.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a customer account")
    public MessageResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        MessageResponse response =
                authService.register(request);

        /*
         * Clear old failed login attempts for this username.
         *
         * This is helpful when someone tried to log in with
         * the username before creating the account.
         */
        loginAttemptService.reset(
                request.username()
        );

        return response;
    }

    /*
     * Login using username and password.
     *
     * Failed login attempts are tracked separately
     * for every username.
     */
    @PostMapping("/login")
    @Operation(summary = "Login as a customer")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        String username = request.username();

        /*
         * Check whether this specific username
         * has reached the failed login limit.
         */
        if (!loginAttemptService.isAllowed(username)) {

            authService.recordRateLimitedLogin(username);

            throw new TooManyRequestsException(
                    "Too many failed login attempts for this "
                            + "username. Try again later."
            );
        }

        try {
            AuthResponse response =
                    authService.login(request);

            /*
             * Successful login clears only this
             * username's failed-attempt counter.
             */
            loginAttemptService.reset(username);

            return response;

        } catch (UnauthorizedException ex) {

            /*
             * Wrong username or password increments only
             * this username's failed-attempt counter.
             */
            loginAttemptService.recordFailure(username);

            throw ex;
        }
    }

    /*
     * Check whether the username exists before
     * continuing with forgot password.
     */
    @PostMapping("/forgot-password/check-user")
    @Operation(summary = "Check whether the username exists")
    public UserExistenceResponse checkUserExists(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return authService.checkUserExists(
                request.username()
        );
    }

    /*
     * Verify that the entered email belongs
     * to the username.
     */
    @PostMapping("/forgot-password/verify")
    @Operation(
            summary =
                    "Verify the username using its registered email"
    )
    public PasswordResetTicketResponse verifyForgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordVerificationRequest request
    ) {
        return authService.verifyForgotPassword(request);
    }

    /*
     * Reset the password using the temporary
     * one-time reset token.
     */
    @PostMapping("/forgot-password/reset")
    @Operation(
            summary =
                    "Reset the password with a short-lived one-time ticket"
    )
    public MessageResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        MessageResponse response =
                authService.resetPassword(request);

        /*
         * After a successful password reset,
         * remove the failed login counter.
         *
         * This requires ResetPasswordRequest to contain
         * the username. Remove this block when your DTO
         * contains only resetToken and newPassword.
         */

        return response;
    }

    /*
     * Generate a new access token and refresh token.
     */
    @PostMapping("/refresh")
    @Operation(
            summary =
                    "Rotate a refresh token and issue a new token pair"
    )
    public AuthResponse refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return authService.refresh(
                request.refreshToken()
        );
    }

    /*
     * Logout and revoke the access and refresh tokens.
     */
    @PostMapping("/logout")
    @Operation(
            summary =
                    "Revoke the supplied access and refresh tokens"
    )
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization,

            @Valid @RequestBody LogoutRequest request
    ) {
        String accessToken =
                JwtAuthenticationFilter.resolveBearerToken(
                        authorization
                );

        authService.logout(
                accessToken,
                request.refreshToken()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Logged out successfully"
                )
        );
    }
}