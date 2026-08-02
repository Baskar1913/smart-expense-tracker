package com.example.expensetracker.repository;

import com.example.expensetracker.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    long deleteByExpiresAtBefore(Instant instant);
}
