package com.example.expensetracker.service;

import com.example.expensetracker.entity.AppUser;
import com.example.expensetracker.entity.PasswordResetToken;
import com.example.expensetracker.exception.UnauthorizedException;
import com.example.expensetracker.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository repository;

    private PasswordResetService service;
    private AppUser user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(repository, 600);
        user = new AppUser("baskar", "baskar@example.com", "hash");
    }

    @Test
    void createTokenShouldReturnRawTokenButStoreOnlyItsHash() {
        ArgumentCaptor<PasswordResetToken> captor =
            ArgumentCaptor.forClass(PasswordResetToken.class);

        String rawToken = service.createToken(user);

        verify(repository).save(captor.capture());
        PasswordResetToken stored = captor.getValue();

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, stored.getTokenHash());
        assertEquals(64, stored.getTokenHash().length());
        assertEquals(user, stored.getUser());
        assertFalse(stored.isUsed());
        assertTrue(stored.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void consumeShouldMarkValidTokenAsUsedAndReturnUser() {
        PasswordResetToken token = new PasswordResetToken(
            "hash", user, Instant.now().plusSeconds(600)
        );
        when(repository.findByTokenHashAndUsedFalse(anyString()))
            .thenReturn(Optional.of(token));

        AppUser result = service.consume("raw-token");

        assertEquals(user, result);
        assertTrue(token.isUsed());
        verify(repository).save(token);
    }

    @Test
    void consumeShouldRejectUnknownToken() {
        when(repository.findByTokenHashAndUsedFalse(anyString()))
            .thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> service.consume("unknown-token")
        );

        assertEquals("Invalid password reset token", exception.getMessage());
    }

    @Test
    void consumeShouldRejectExpiredToken() {
        PasswordResetToken token = new PasswordResetToken(
            "hash", user, Instant.now().minusSeconds(1)
        );
        when(repository.findByTokenHashAndUsedFalse(anyString()))
            .thenReturn(Optional.of(token));

        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> service.consume("expired-token")
        );

        assertEquals("Password reset token has expired", exception.getMessage());
        assertFalse(token.isUsed());
    }

    @Test
    void expirationSecondsShouldBeReturned() {
        assertEquals(600, service.getExpirationSeconds());
    }
}
