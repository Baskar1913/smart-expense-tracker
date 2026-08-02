package com.example.expensetracker.controller;

import com.example.expensetracker.dto.auth.MessageResponse;
import com.example.expensetracker.dto.expense.*;
import com.example.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an expense for the authenticated customer")
    public ExpenseResponse add(@Valid @RequestBody ExpenseRequest request, Authentication authentication) {
        return expenseService.addExpense(authentication.getName(), request);
    }

    @GetMapping
    @Operation(summary = "List the authenticated customer's expenses, optionally filtered by category")
    public List<ExpenseResponse> list(
        @RequestParam(required = false) String category,
        Authentication authentication
    ) {
        return expenseService.list(authentication.getName(), category);
    }

    @GetMapping("/search")
    @Operation(summary = "Search the authenticated customer's expenses")
    public List<ExpenseResponse> search(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) BigDecimal minAmount,
        @RequestParam(required = false) BigDecimal maxAmount,
        Authentication authentication
    ) {
        return expenseService.search(authentication.getName(), query, category, from, to, minAmount, maxAmount);
    }

    @GetMapping("/total")
    @Operation(summary = "Calculate the authenticated customer's total, optionally for one category")
    public TotalResponse total(
        @RequestParam(required = false) String category,
        Authentication authentication
    ) {
        return expenseService.total(authentication.getName(), category);
    }

    @GetMapping("/total/by-category")
    @Operation(summary = "Calculate the authenticated customer's totals grouped by category")
    public List<CategoryTotalResponse> totalsByCategory(Authentication authentication) {
        return expenseService.totalsByCategory(authentication.getName());
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = "Return a monthly summary for the authenticated customer")
    public MonthlySummaryResponse monthlySummary(
        @RequestParam int year,
        @RequestParam int month,
        Authentication authentication
    ) {
        return expenseService.monthlySummary(authentication.getName(), year, month);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense owned by the authenticated customer")
    public MessageResponse delete(@PathVariable Long id, Authentication authentication) {
        expenseService.deleteExpense(id, authentication.getName());
        return new MessageResponse("Expense deleted successfully");
    }
}
