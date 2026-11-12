package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.port.in.RiesgoCommandHandler;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.infrastructure.config.ApiKeyFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiesgoController.class)
@Import({ApiKeyFilter.class, GlobalExceptionHandler.class})
@DisplayName("Pruebas Unitarias de Capa Web: RiesgoController")
class RiesgoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiesgoCommandHandler riesgoCommandHandler;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    @Test
    @DisplayName("POST /api/v1/riesgos/{id}/cancelar cancela riesgo existente y retorna 200 OK")
    void testCancelarRiesgoExitoso() throws Exception {
        Riesgo riesgo = new Riesgo(null, EstadoRiesgo.CANCELADO, "Calle 100", "Bogota", "Inquilino", BigDecimal.valueOf(1000));
        riesgo.setId(10L);

        when(riesgoCommandHandler.cancelarRiesgo(any())).thenReturn(riesgo);

        mockMvc.perform(post("/api/v1/riesgos/10/cancelar")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    @DisplayName("POST /api/v1/riesgos/{id}/cancelar con ID inexistente retorna 404 Not Found")
    void testCancelarRiesgoNoExisteRetorna404() throws Exception {
        when(riesgoCommandHandler.cancelarRiesgo(any()))
                .thenThrow(new PolizaNotFoundException("Riesgo no encontrado con ID: 999"));

        mockMvc.perform(post("/api/v1/riesgos/999/cancelar")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso no encontrado")));
    }
}
