package com.example.expensetracker.security;

import com.example.expensetracker.exception.ApiError;
import com.example.expensetracker.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper, AuditService auditService) {
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {
        auditService.record(
            request,
            null,
            "AUTHENTICATION_REQUIRED",
            "FAILURE",
            "SECURITY",
            null,
            "Protected endpoint was requested without a valid customer access token"
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            new ApiError(
                Instant.now(),
                401,
                "Unauthorized",
                "A valid customer access token is required",
                request.getRequestURI(),
                Map.of()
            )
        );
    }
}
