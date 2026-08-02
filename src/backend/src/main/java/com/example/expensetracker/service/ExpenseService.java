package com.example.expensetracker.service;

import com.example.expensetracker.dto.expense.*;
import com.example.expensetracker.entity.AppUser;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        UserRepository userRepository,
        AuditService auditService
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ExpenseResponse addExpense(String username, ExpenseRequest request) {
        validateFourDigitYear(request.date(), "Expense date");
        AppUser user = requireUser(username);
        Expense saved = expenseRepository.save(new Expense(
            request.title().trim(),
            request.amount(),
            normalizeCategory(request.category()),
            request.date(),
            user
        ));
        auditService.record(username, "EXPENSE_CREATE", "SUCCESS", "EXPENSE", saved.getId().toString(), "Expense created");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(String username, String category) {
        AppUser user = requireUser(username);
        List<ExpenseResponse> results = ownExpenses(user).stream()
            .filter(expense -> category == null || category.isBlank()
                || expense.getCategory().equalsIgnoreCase(category.trim()))
            .map(this::toResponse)
            .toList();
        auditService.record(username, "EXPENSE_LIST", "SUCCESS", "EXPENSE", null, "Returned " + results.size() + " own expense records");
        return results;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> search(
        String username,
        String query,
        String category,
        LocalDate from,
        LocalDate to,
        BigDecimal minAmount,
        BigDecimal maxAmount
    ) {
        validateFourDigitYear(from, "'from' date");
        validateFourDigitYear(to, "'to' date");
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }

        String normalizedQuery = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null ? null : category.trim();

        List<ExpenseResponse> results = ownExpenses(requireUser(username)).stream()
            .filter(expense -> normalizedQuery == null || normalizedQuery.isBlank()
                || expense.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || expense.getCategory().toLowerCase(Locale.ROOT).contains(normalizedQuery))
            .filter(expense -> normalizedCategory == null || normalizedCategory.isBlank()
                || expense.getCategory().equalsIgnoreCase(normalizedCategory))
            .filter(expense -> from == null || !expense.getDate().isBefore(from))
            .filter(expense -> to == null || !expense.getDate().isAfter(to))
            .filter(expense -> minAmount == null || expense.getAmount().compareTo(minAmount) >= 0)
            .filter(expense -> maxAmount == null || expense.getAmount().compareTo(maxAmount) <= 0)
            .map(this::toResponse)
            .toList();

        auditService.record(username, "EXPENSE_SEARCH", "SUCCESS", "EXPENSE", null, "Search returned " + results.size() + " own expense records");
        return results;
    }

    @Transactional(readOnly = true)
    public TotalResponse total(String username, String category) {
        BigDecimal total = ownExpenses(requireUser(username)).stream()
            .filter(expense -> category == null || category.isBlank()
                || expense.getCategory().equalsIgnoreCase(category.trim()))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        auditService.record(username, "EXPENSE_TOTAL", "SUCCESS", "EXPENSE", null, "Calculated own expense total");
        return new TotalResponse(total);
    }

    @Transactional(readOnly = true)
    public List<CategoryTotalResponse> totalsByCategory(String username) {
        Map<String, BigDecimal> totals = ownExpenses(requireUser(username)).stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));
        auditService.record(username, "EXPENSE_TOTAL_BY_CATEGORY", "SUCCESS", "EXPENSE", null, "Calculated own totals by category");
        return totals.entrySet().stream()
            .map(entry -> new CategoryTotalResponse(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(String username, int year, int month) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid year or month");
        }

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Expense> monthly = ownExpenses(requireUser(username)).stream()
            .filter(expense -> !expense.getDate().isBefore(start) && !expense.getDate().isAfter(end))
            .toList();

        BigDecimal total = monthly.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryTotals = monthly.stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));

        auditService.record(username, "MONTHLY_SUMMARY", "SUCCESS", "EXPENSE", null, "Viewed monthly summary for " + yearMonth);
        return new MonthlySummaryResponse(year, month, monthly.size(), total, categoryTotals);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        AppUser user = requireUser(username);
        Expense expense = expenseRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> {
                auditService.record(username, "EXPENSE_DELETE", "FAILURE", "EXPENSE", id.toString(), "Expense not found or not owned by customer");
                return new ResourceNotFoundException("Expense not found: " + id);
            });
        expenseRepository.delete(expense);
        auditService.record(username, "EXPENSE_DELETE", "SUCCESS", "EXPENSE", id.toString(), "Own expense deleted");
    }

    private void validateFourDigitYear(LocalDate date, String fieldName) {
        if (date == null) {
            return;
        }
        int year = date.getYear();
        if (year < 1000 || year > 9999) {
            throw new IllegalArgumentException(fieldName + " must contain a four-digit year");
        }
    }

    private AppUser requireUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private List<Expense> ownExpenses(AppUser user) {
        return expenseRepository.findAllByUserOrderByDateDescIdDesc(user);
    }

    private String normalizeCategory(String category) {
        String cleaned = category.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        return Arrays.stream(cleaned.toLowerCase(Locale.ROOT).split(" "))
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
            .collect(Collectors.joining(" "));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getTitle(),
            expense.getAmount(),
            expense.getCategory(),
            expense.getDate()
        );
    }
}
