package com.example.expensetracker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Reset token is required")
    String resetToken,

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72, message = "New password must contain 8 to 72 characters")
    String newPassword
) {
}
