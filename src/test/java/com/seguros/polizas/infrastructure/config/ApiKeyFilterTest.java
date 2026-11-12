package com.seguros.polizas.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Seguridad: ApiKeyFilter")
class ApiKeyFilterTest {

    private ApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyFilter();
        ReflectionTestUtils.setField(filter, "apiKeyHeader", "x-api-key");
        ReflectionTestUtils.setField(filter, "expectedApiKey", "123456");
    }

    @Test
    @DisplayName("Petición sin header x-api-key es rechazada con 401 Unauthorized y JSON descriptivo")
    void testFaltaApiKeyRetorna401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("No autorizado"));
        assertTrue(response.getContentAsString().contains("invalido o ausente"));
    }

    @Test
    @DisplayName("Petición con header x-api-key erróneo es rechazada con 401 Unauthorized")
    void testApiKeyErroneoRetorna401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        request.addHeader("x-api-key", "clave-invalida");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("Petición con header x-api-key válido 123456 continúa la cadena de filtros exitosamente")
    void testApiKeyValidoPermiteAcceso() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        request.addHeader("x-api-key", "123456");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("Rutas especiales como /h2-console y Swagger (/swagger-ui, /v3/api-docs) se omiten de la validación")
    void testRutasOmitidas() {
        MockHttpServletRequest reqConsole = new MockHttpServletRequest("GET", "/h2-console/login.do");
        assertTrue(filter.shouldNotFilter(reqConsole));

        MockHttpServletRequest reqActuator = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(reqActuator));

        MockHttpServletRequest reqSwaggerUi = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(reqSwaggerUi));

        MockHttpServletRequest reqApiDocs = new MockHttpServletRequest("GET", "/v3/api-docs/swagger-config");
        assertTrue(filter.shouldNotFilter(reqApiDocs));

        MockHttpServletRequest reqApi = new MockHttpServletRequest("GET", "/polizas/1/renovar");
        assertFalse(filter.shouldNotFilter(reqApi));
    }
}

