package com.example.expensetracker.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes developer-only security and business audit events to backend/logs/audit.log.
 * Sensitive values such as passwords and tokens must never be passed to this service.
 */
@Service
public class AuditService {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void record(
        String username,
        String action,
        String outcome,
        String resourceType,
        String resourceId,
        String details
    ) {
        record(currentRequest(), username, action, outcome, resourceType, resourceId, details);
    }

    public void record(
        HttpServletRequest request,
        String username,
        String action,
        String outcome,
        String resourceType,
        String resourceId,
        String details
    ) {
        String requestId = safe(MDC.get("requestId"), 80);
        String ipAddress = request == null ? null : safe(request.getRemoteAddr(), 64);
        String method = request == null ? null : safe(request.getMethod(), 12);
        String endpoint = request == null ? null : safe(request.getRequestURI(), 240);

        Object[] values = {
            safe(username, 80),
            safe(action, 80),
            safe(outcome, 20),
            requestId,
            method,
            endpoint,
            safe(resourceType, 80),
            safe(resourceId, 80),
            ipAddress,
            safe(details, 1000)
        };

        String template =
            "user={} action={} outcome={} requestId={} method={} endpoint={} "
                + "resourceType={} resourceId={} ip={} details={}";

        if ("FAILURE".equalsIgnoreCase(outcome)) {
            auditLogger.warn(template, values);
        } else {
            auditLogger.info(template, values);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String safe(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String cleaned = value.replaceAll("[\\r\\n\\t]", " ").trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
