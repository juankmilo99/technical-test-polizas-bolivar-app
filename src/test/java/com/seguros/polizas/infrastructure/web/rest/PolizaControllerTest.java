package com.seguros.polizas.infrastructure.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.application.port.in.PolizaCommandHandler;
import com.seguros.polizas.application.port.in.PolizaQuery;
import com.seguros.polizas.application.port.in.RiesgoCommandHandler;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.infrastructure.config.ApiKeyFilter;
import com.seguros.polizas.infrastructure.web.rest.dto.PolizaRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RenovacionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolizaController.class)
@Import({ApiKeyFilter.class, GlobalExceptionHandler.class})
@DisplayName("Pruebas Unitarias de Capa Web: PolizaController")
class PolizaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolizaCommandHandler polizaCommandHandler;

    @MockBean
    private RiesgoCommandHandler riesgoCommandHandler;

    @MockBean
    private PolizaQuery polizaQuery;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    private Poliza crearPolizaFixture(Long id, TipoPoliza tipo, EstadoPoliza estado) {
        Poliza p = new Poliza(
                "POL-MOCK-" + id, tipo, estado,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 12,
                BigDecimal.valueOf(1500000.00),
                "Tomador Test", "Asegurado Test", "Beneficiario Test");
        p.setId(id);
        return p;
    }

    @Test
    @DisplayName("GET /api/v1/polizas lista correctamente con status 200")
    void testListarPolizas() throws Exception {
        when(polizaQuery.listar(any(), any()))
                .thenReturn(List.of(crearPolizaFixture(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE)));

        mockMvc.perform(get("/api/v1/polizas")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].numeroPoliza", is("POL-MOCK-1")))
                .andExpect(jsonPath("$[0].tipo", is("INDIVIDUAL")));
    }

    @Test
    @DisplayName("GET /api/v1/polizas/{id} retorna 404 cuando la póliza no existe")
    void testObtenerPorIdNoExisteRetorna404() throws Exception {
        when(polizaQuery.obtenerPorId(999L))
                .thenThrow(new PolizaNotFoundException("Poliza no encontrada con ID: 999"));

        mockMvc.perform(get("/api/v1/polizas/999")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso no encontrado")));
    }

    @Test
    @DisplayName("POST /api/v1/polizas con payload inválido retorna 400 Bad Request")
    void testCrearPolizaValidacionFallaRetorna400() throws Exception {
        PolizaRequest invalidReq = new PolizaRequest();

        mockMvc.perform(post("/api/v1/polizas")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validacion de entrada fallida")));
    }

    @Test
    @DisplayName("POST /api/v1/polizas/{id}/renovar con IPC inválido retorna 400 Bad Request")
    void testRenovarPolizaIpcInvalidoRetorna400() throws Exception {
        RenovacionRequest invalidReq = new RenovacionRequest(BigDecimal.valueOf(-1.0));

        mockMvc.perform(post("/api/v1/polizas/1/renovar")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/v1/polizas/{id}/renovar en póliza cancelada mapea BusinessRuleException a 422")
    void testRenovarPolizaCanceladaRetorna422() throws Exception {
        RenovacionRequest req = new RenovacionRequest(BigDecimal.valueOf(5.10));

        when(polizaCommandHandler.renovar(any()))
                .thenThrow(new BusinessRuleException("No se puede renovar una poliza en estado CANCELADA."));

        mockMvc.perform(post("/api/v1/polizas/3/renovar")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    @DisplayName("POST /api/v1/polizas/{id}/cancelar retorna póliza cancelada con status 200")
    void testCancelarPolizaRetorna200() throws Exception {
        Poliza cancelada = crearPolizaFixture(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.CANCELADA);

        when(polizaCommandHandler.cancelar(any())).thenReturn(cancelada);

        mockMvc.perform(post("/api/v1/polizas/1/cancelar")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")));
    }
}
