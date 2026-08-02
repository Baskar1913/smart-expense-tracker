package com.example.expensetracker.security;

import com.example.expensetracker.dto.auth.AuthResponse;
import com.example.expensetracker.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(
        @Value("${app.jwt.secret}") String base64Secret,
        @Value("${app.jwt.access-expiration-seconds}") long accessExpirationSeconds,
        @Value("${app.jwt.refresh-expiration-seconds}") long refreshExpirationSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public AuthResponse createTokenPair(AppUser user) {
        String access = createToken(user, TokenType.ACCESS, accessExpirationSeconds);
        String refresh = createToken(user, TokenType.REFRESH, refreshExpirationSeconds);
        return new AuthResponse(
            access,
            refresh,
            "Bearer",
            accessExpirationSeconds,
            user.getUsername()
        );
    }

    public Claims parseAndValidate(String token, TokenType expectedType) {
        Claims claims = parse(token);
        String type = claims.get("type", String.class);
        if (!expectedType.name().equals(type)) {
            throw new JwtException("Unexpected token type");
        }
        return claims;
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private String createToken(AppUser user, TokenType tokenType, long lifetimeSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(lifetimeSeconds);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("type", tokenType.name())
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact();
    }
}
