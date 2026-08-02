package com.example.expensetracker.dto.expense;

import java.math.BigDecimal;

public record CategoryTotalResponse(String category, BigDecimal total) {
}
