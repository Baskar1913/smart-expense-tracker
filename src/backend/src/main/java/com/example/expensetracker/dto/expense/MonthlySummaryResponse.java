package com.example.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlySummaryResponse(
    int year,
    int month,
    long expenseCount,
    BigDecimal total,
    Map<String, BigDecimal> totalsByCategory
) {
}
