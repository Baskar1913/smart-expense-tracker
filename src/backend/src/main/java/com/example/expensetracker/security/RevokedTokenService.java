package com.example.expensetracker.security;

import com.example.expensetracker.entity.RevokedToken;
import com.example.expensetracker.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RevokedTokenService {
    private static final Logger log = LoggerFactory.getLogger(RevokedTokenService.class);

    private final RevokedTokenRepository repository;
    private final JwtService jwtService;

    public RevokedTokenService(RevokedTokenRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public boolean isRevoked(String jti) {
        return jti != null && repository.existsById(jti);
    }

    @Transactional
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        try {
            Claims claims = jwtService.parse(token);
            if (claims.getId() != null && claims.getExpiration() != null) {
                repository.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
            }
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token could not be added to revocation list: {}", ex.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.jwt.revocation-cleanup-ms:3600000}")
    @Transactional
    public void removeExpiredRevocations() {
        long deleted = repository.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("Removed {} expired revoked-token entries", deleted);
        }
    }
}
