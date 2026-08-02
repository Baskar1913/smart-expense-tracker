package com.example.expensetracker.dto.auth;

public record UserExistenceResponse(
    boolean exists,
    String message
) {
}
