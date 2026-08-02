package com.example.expensetracker.service;

import com.example.expensetracker.dto.auth.*;
import com.example.expensetracker.entity.AppUser;
import com.example.expensetracker.exception.UnauthorizedException;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtService;
import com.example.expensetracker.security.RevokedTokenService;
import com.example.expensetracker.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;
    private final PasswordResetService passwordResetService;
    private final AuditService auditService;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        RevokedTokenService revokedTokenService,
        PasswordResetService passwordResetService,
        AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokenService = revokedTokenService;
        this.passwordResetService = passwordResetService;
        this.auditService = auditService;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            auditService.record(username, "ACCOUNT_CREATE", "FAILURE", "USER", null, "Username already exists");
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            auditService.record(username, "ACCOUNT_CREATE", "FAILURE", "USER", null, "Email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser saved = userRepository.save(new AppUser(
            username,
            email,
            passwordEncoder.encode(request.password())
        ));
        auditService.record(username, "ACCOUNT_CREATE", "SUCCESS", "USER", saved.getId().toString(), "Customer account created");
        return new MessageResponse("Account created successfully. Please sign in.");
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (AuthenticationException ex) {
            auditService.record(username, "LOGIN", "FAILURE", "USER", null, "Invalid username or password");
            throw new UnauthorizedException("Invalid username or password");
        }

        AppUser user = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        auditService.record(user.getUsername(), "LOGIN", "SUCCESS", "USER", user.getId().toString(), "Customer logged in");
        return jwtService.createTokenPair(user);
    }


    public void recordRateLimitedLogin(String usernameInput) {
        String username = usernameInput == null || usernameInput.isBlank()
            ? null
            : normalizeUsername(usernameInput);
        auditService.record(
            username,
            "LOGIN_RATE_LIMITED",
            "FAILURE",
            "AUTH",
            null,
            "Login blocked because too many failed attempts were recorded"
        );
    }

    public UserExistenceResponse checkUserExists(String usernameInput) {
        String username = normalizeUsername(usernameInput);
        boolean exists = userRepository.existsByUsernameIgnoreCase(username);
        auditService.record(
            username,
            "FORGOT_PASSWORD_USER_CHECK",
            exists ? "SUCCESS" : "FAILURE",
            "USER",
            null,
            exists ? "Username exists" : "Username does not exist"
        );

        String message = exists
            ? "Username exists. Verify your registered email to continue."
            : "Username does not exist. Create an account first.";
        return new UserExistenceResponse(exists, message);
    }

    public PasswordResetTicketResponse verifyForgotPassword(ForgotPasswordVerificationRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByUsernameIgnoreCaseAndEmailIgnoreCase(username, email)
            .orElseThrow(() -> {
                auditService.record(username, "FORGOT_PASSWORD_VERIFY", "FAILURE", "USER", null, "Username and email did not match");
                return new UnauthorizedException("Username and registered email do not match");
            });

        String resetToken = passwordResetService.createToken(user);
        auditService.record(username, "FORGOT_PASSWORD_VERIFY", "SUCCESS", "USER", user.getId().toString(), "Registered email verified; reset ticket issued");
        return new PasswordResetTicketResponse(
            resetToken,
            passwordResetService.getExpirationSeconds(),
            "Identity verified. Set a new password."
        );
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        AppUser user;
        try {
            user = passwordResetService.consume(request.resetToken());
        } catch (UnauthorizedException ex) {
            auditService.record(null, "PASSWORD_RESET", "FAILURE", "USER", null, ex.getMessage());
            throw ex;
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.record(user.getUsername(), "PASSWORD_RESET", "SUCCESS", "USER", user.getId().toString(), "Password changed using a one-time reset ticket");
        return new MessageResponse("Password reset successfully. Please sign in with your new password.");
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            Claims claims = jwtService.parseAndValidate(refreshToken, TokenType.REFRESH);
            if (revokedTokenService.isRevoked(claims.getId())) {
                throw new UnauthorizedException("Refresh token has been revoked");
            }

            AppUser user = userRepository.findByUsernameIgnoreCase(claims.getSubject())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

            revokedTokenService.revoke(refreshToken);
            auditService.record(user.getUsername(), "TOKEN_REFRESH", "SUCCESS", "USER", user.getId().toString(), "Refresh token rotated");
            return jwtService.createTokenPair(user);
        } catch (UnauthorizedException ex) {
            auditService.record(null, "TOKEN_REFRESH", "FAILURE", "TOKEN", null, ex.getMessage());
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            auditService.record(null, "TOKEN_REFRESH", "FAILURE", "TOKEN", null, "Invalid or expired refresh token");
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    public void logout(String accessToken, String refreshToken) {
        String username = null;
        try {
            if (accessToken != null && !accessToken.isBlank()) {
                username = jwtService.parse(accessToken).getSubject();
            }
        } catch (RuntimeException ignored) {
            // Logout still revokes any token that can be parsed by RevokedTokenService.
        }

        revokedTokenService.revoke(accessToken);
        revokedTokenService.revoke(refreshToken);
        auditService.record(username, "LOGOUT", "SUCCESS", "TOKEN", null, "Supplied tokens revoked");
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
