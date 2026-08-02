package com.example.expensetracker.dto.auth;

public record PasswordResetTicketResponse(
    String resetToken,
    long expiresInSeconds,
    String message
) {
}
