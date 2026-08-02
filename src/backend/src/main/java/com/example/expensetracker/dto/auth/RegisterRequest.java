package com.example.expensetracker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 80, message = "Username must contain 3 to 80 characters")
    @Pattern(
        regexp = "^[A-Za-z0-9._-]+$",
        message = "Username may contain letters, numbers, dot, underscore, and hyphen only"
    )
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 160, message = "Email must not exceed 160 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must contain 8 to 72 characters")
    String password
) {
}
