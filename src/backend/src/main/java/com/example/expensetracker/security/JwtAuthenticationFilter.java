package com.example.expensetracker.security;

import com.example.expensetracker.service.AuditService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        RevokedTokenService revokedTokenService,
        UserDetailsService userDetailsService,
        AuditService auditService
    ) {
        this.jwtService = jwtService;
        this.revokedTokenService = revokedTokenService;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveBearerToken(request.getHeader("Authorization"));

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parseAndValidate(token, TokenType.ACCESS);
                if (revokedTokenService.isRevoked(claims.getId())) {
                    throw new JwtException("Token has been revoked");
                }

                String username = claims.getSubject();
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("JWT rejected for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());

                auditService.record(
                    request,
                    null,
                    "JWT_REJECTED",
                    "FAILURE",
                    "TOKEN",
                    null,
                    "Access token rejected: " + ex.getClass().getSimpleName()
                );
            }
        }

        filterChain.doFilter(request, response);
    }

    public static String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}
