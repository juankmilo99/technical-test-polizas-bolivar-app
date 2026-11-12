package com.seguros.polizas.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.security.api-key-header:x-api-key}")
    private String apiKeyHeader;

    @Value("${app.security.api-key-value:123456}")
    private String expectedApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Rutas excluidas de validación estricta para diagnóstico, observabilidad y consola
        return path.startsWith("/h2-console") ||
               path.startsWith("/actuator") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/error") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String headerValue = request.getHeader(apiKeyHeader);

        if (headerValue == null || !headerValue.trim().equals(expectedApiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"No autorizado\",\"message\":\"Header '" +
                    apiKeyHeader + "' invalido o ausente. Se requiere la clave de autorizacion autorizada.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}

