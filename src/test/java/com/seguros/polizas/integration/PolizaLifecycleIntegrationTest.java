package com.seguros.polizas.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.infrastructure.web.rest.dto.PolizaRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RenovacionRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiesgoRequest;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Prueba de Integración End-to-End: Ciclo de Vida Completo de Pólizas y Riesgos")
class PolizaLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoreIntegrationPort coreService;

    private static final String API_KEY = "123456";

    @Test
    @DisplayName("Flujo Completo E2E: Creación Colectiva -> Inclusión Múltiple -> Cancelación Riesgo -> Renovación IPC -> Cancelación Póliza -> Invariantes")
    void testCicloDeVidaCompletoPolizaColectiva() throws Exception {
        // 1. Crear una Póliza Colectiva
        PolizaRequest crearReq = new PolizaRequest();
        crearReq.setNumeroPoliza("POL-COL-E2E-999");
        crearReq.setTipo(TipoPoliza.COLECTIVA);
        crearReq.setFechaInicio(LocalDate.of(2026, 1, 1));
        crearReq.setMesesVigencia(12);
        crearReq.setCanonMensual(new BigDecimal("5000000.00"));
        crearReq.setTomador("Inmobiliaria Central");
        crearReq.setAsegurado("Varios Arrendatarios");
        crearReq.setBeneficiario("Arrendadores Unidos");

        MvcResult resultCrear = mockMvc.perform(post("/api/v1/polizas")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.estado", is("VIGENTE")))
                .andExpect(jsonPath("$.primaTotal", is(60000000.00)))
                .andReturn();

        JsonNode jsonPoliza = objectMapper.readTree(resultCrear.getResponse().getContentAsString());
        long polizaId = jsonPoliza.get("id").asLong();

        // 2. Agregar Riesgo 1
        RiesgoRequest riesgo1 = new RiesgoRequest("Avenida Chile # 72-10", "Bogota", "Tenant Alpha", new BigDecimal("2500000.00"));
        MvcResult resR1 = mockMvc.perform(post("/api/v1/polizas/" + polizaId + "/riesgos")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(riesgo1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("ACTIVO")))
                .andReturn();
        long r1Id = objectMapper.readTree(resR1.getResponse().getContentAsString()).get("id").asLong();

        // 3. Agregar Riesgo 2
        RiesgoRequest riesgo2 = new RiesgoRequest("Calle 72 # 10-34", "Bogota", "Tenant Beta", new BigDecimal("2500000.00"));
        mockMvc.perform(post("/api/v1/polizas/" + polizaId + "/riesgos")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(riesgo2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("ACTIVO")));

        // 4. Consultar Riesgos de la Póliza
        mockMvc.perform(get("/api/v1/polizas/" + polizaId + "/riesgos")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Cancelar individualmente Riesgo 1
        mockMvc.perform(post("/api/v1/riesgos/" + r1Id + "/cancelar")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));

        // 6. Renovar la Póliza Colectiva con IPC del 5.10%
        RenovacionRequest renReq = new RenovacionRequest(new BigDecimal("5.10"));
        mockMvc.perform(post("/api/v1/polizas/" + polizaId + "/renovar")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("RENOVADA")))
                // 5,000,000 * 1.051 = 5,255,000.00
                .andExpect(jsonPath("$.canonMensual", is(5255000.00)))
                // 5,255,000 * 12 = 63,060,000.00
                .andExpect(jsonPath("$.primaTotal", is(63060000.00)));

        // 7. Cancelar la Póliza Colectiva en cascada
        mockMvc.perform(post("/api/v1/polizas/" + polizaId + "/cancelar")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")));

        // 8. Verificar que todos sus riesgos quedaron en CANCELADO
        MvcResult resRiesgosFin = mockMvc.perform(get("/api/v1/polizas/" + polizaId + "/riesgos")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode riesgosFinArr = objectMapper.readTree(resRiesgosFin.getResponse().getContentAsString());
        for (JsonNode r : riesgosFinArr) {
            assertEquals("CANCELADO", r.get("estado").asText());
        }

        // 9. Verificar Invariante: No se puede renovar una póliza cancelada
        mockMvc.perform(post("/api/v1/polizas/" + polizaId + "/renovar")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }
}

