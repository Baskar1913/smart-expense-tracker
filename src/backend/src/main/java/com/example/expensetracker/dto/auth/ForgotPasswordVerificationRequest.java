package com.example.expensetracker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordVerificationRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Registered email is required")
    @Email(message = "Enter a valid email address")
    String email
) {
}
