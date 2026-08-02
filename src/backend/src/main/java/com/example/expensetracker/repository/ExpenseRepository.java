package com.example.expensetracker.repository;

import com.example.expensetracker.entity.AppUser;
import com.example.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByUserOrderByDateDescIdDesc(AppUser user);
    Optional<Expense> findByIdAndUser(Long id, AppUser user);
}
