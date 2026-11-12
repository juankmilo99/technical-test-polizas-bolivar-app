package com.seguros.polizas.infrastructure.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Observabilidad: CorrelationIdFilter")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("Genera un Correlation ID nuevo cuando el request no lo envía")
    void testGeneraNuevoCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/policies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String generatedId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertNotNull(generatedId);
        assertFalse(generatedId.trim().isEmpty());
    }

    @Test
    @DisplayName("Preserva el Correlation ID enviado por el cliente en el header X-Correlation-Id")
    void testPreservaCorrelationIdExistente() throws ServletException, IOException {
        String existingId = "trace-abc-123-xyz";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/policies");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(existingId, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }
}

