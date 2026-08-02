package com.example.expensetracker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
    @NotBlank(message = "Username is required")
    @Size(max = 80, message = "Username must not exceed 80 characters")
    String username
) {
}
