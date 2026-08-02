package com.example.expensetracker.security;

import com.example.expensetracker.dto.auth.AuthResponse;
import com.example.expensetracker.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private AppUser user;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)
        );
        jwtService = new JwtService(secret, 900, 604800);
        user = new AppUser("baskar", "baskar@example.com", "hash");
    }

    @Test
    void createTokenPairShouldCreateDifferentAccessAndRefreshTokens() {
        AuthResponse response = jwtService.createTokenPair(user);

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotEquals(response.accessToken(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.accessTokenExpiresInSeconds());
        assertEquals("baskar", response.username());
    }

    @Test
    void accessTokenShouldContainUsernameAndAccessType() {
        AuthResponse response = jwtService.createTokenPair(user);

        Claims claims = jwtService.parseAndValidate(
            response.accessToken(), TokenType.ACCESS
        );

        assertEquals("baskar", claims.getSubject());
        assertEquals("ACCESS", claims.get("type", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void refreshTokenShouldContainRefreshType() {
        AuthResponse response = jwtService.createTokenPair(user);

        Claims claims = jwtService.parseAndValidate(
            response.refreshToken(), TokenType.REFRESH
        );

        assertEquals("baskar", claims.getSubject());
        assertEquals("REFRESH", claims.get("type", String.class));
    }

    @Test
    void refreshTokenShouldNotValidateAsAccessToken() {
        AuthResponse response = jwtService.createTokenPair(user);

        assertThrows(
            JwtException.class,
            () -> jwtService.parseAndValidate(
                response.refreshToken(), TokenType.ACCESS
            )
        );
    }

    @Test
    void tokenSignedWithDifferentSecretShouldBeRejected() {
        AuthResponse response = jwtService.createTokenPair(user);

        String otherSecret = Base64.getEncoder().encodeToString(
            "abcdefghijklmnopqrstuvwxyz123456".getBytes(StandardCharsets.UTF_8)
        );
        JwtService otherService = new JwtService(otherSecret, 900, 604800);

        assertThrows(
            JwtException.class,
            () -> otherService.parse(response.accessToken())
        );
    }
}
