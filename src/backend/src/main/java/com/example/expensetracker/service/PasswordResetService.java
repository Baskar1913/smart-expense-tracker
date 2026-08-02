package com.example.expensetracker.service;

import com.example.expensetracker.entity.AppUser;
import com.example.expensetracker.entity.PasswordResetToken;
import com.example.expensetracker.exception.UnauthorizedException;
import com.example.expensetracker.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository repository;
    private final long expirationSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
        PasswordResetTokenRepository repository,
        @Value("${app.password-reset.expiration-seconds:600}") long expirationSeconds
    ) {
        this.repository = repository;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public String createToken(AppUser user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        repository.save(new PasswordResetToken(
            sha256(rawToken),
            user,
            Instant.now().plusSeconds(expirationSeconds)
        ));
        return rawToken;
    }

    @Transactional
    public AppUser consume(String rawToken) {
        PasswordResetToken token = repository.findByTokenHashAndUsedFalse(sha256(rawToken))
            .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Password reset token has expired");
        }

        token.markUsed();
        repository.save(token);
        return token.getUser();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
