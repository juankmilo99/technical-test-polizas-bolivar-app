package com.seguros.polizas;

import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PolizasApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreIntegrationPort coreService;

    @Test
    @DisplayName("Seguridad: Request sin header x-api-key retorna 401 Unauthorized")
    void testAccesoSinApiKeyRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("No autorizado")));
    }

    @Test
    @DisplayName("Seguridad: Request con header x-api-key valido retorna 200 OK")
    void testAccesoConApiKeyValidoRetorna200() throws Exception {
        mockMvc.perform(get("/api/v1/polizas")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Endpoint 1: Listar pólizas con filtros tipo y estado")
    void testListarPolizasConFiltros() throws Exception {
        mockMvc.perform(get("/api/v1/polizas")
                        .param("tipo", "INDIVIDUAL")
                        .param("estado", "VIGENTE")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo", is("INDIVIDUAL")))
                .andExpect(jsonPath("$[0].estado", is("VIGENTE")));
    }

    @Test
    @DisplayName("Endpoint 2: Listar riesgos de póliza existente")
    void testListarRiesgosDePoliza() throws Exception {
        mockMvc.perform(get("/api/v1/polizas/1/riesgos")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Endpoint 3: Renovar póliza vigente incrementa valores y retorna estado RENOVADA")
    void testRenovarPolizaVigente() throws Exception {
        String jsonRequest = "{\"ipc\": 5.10}";

        mockMvc.perform(post("/api/v1/polizas/1/renovar")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("RENOVADA")))
                .andExpect(jsonPath("$.canonMensual", notNullValue()))
                .andExpect(jsonPath("$.primaTotal", notNullValue()));

        verify(coreService, times(1)).notificarEvento(eq("RENOVACION"), eq(1L));
    }

    @Test
    @DisplayName("Regla de Negocio: No se puede renovar póliza cancelada (retorna 422)")
    void testRenovarPolizaCanceladaRetorna422() throws Exception {
        String jsonRequest = "{\"ipc\": 5.10}";

        mockMvc.perform(post("/api/v1/polizas/3/renovar")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Violacion de regla de negocio")));
    }

    @Test
    @DisplayName("Regla de Negocio: Agregar riesgo a póliza individual retorna 422")
    void testAgregarRiesgoAPolizaIndividualRetorna422() throws Exception {
        String jsonRequest = "{"
                + "\"direccionInmueble\": \"Calle 50 # 10-20\","
                + "\"ciudad\": \"Bogota\","
                + "\"nombreAsegurado\": \"Nuevo Inquilino\","
                + "\"valorAsegurado\": 1800000.00"
                + "}";

        mockMvc.perform(post("/api/v1/polizas/1/riesgos")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Violacion de regla de negocio")));
    }

    @Test
    @DisplayName("Endpoint 5: Agregar riesgo a póliza colectiva retorna 201 Created")
    void testAgregarRiesgoAPolizaColectivaRetorna201() throws Exception {
        String jsonRequest = "{"
                + "\"direccionInmueble\": \"Avenida Siempre Viva 742\","
                + "\"ciudad\": \"Bogota\","
                + "\"nombreAsegurado\": \"Empresa XYZ\","
                + "\"valorAsegurado\": 3500000.00"
                + "}";

        mockMvc.perform(post("/api/v1/polizas/2/riesgos")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));

        verify(coreService, times(1)).notificarEvento(eq("INCLUSION_RIESGO"), eq(2L));
    }

    @Test
    @DisplayName("Mock CORE: POST /core-mock/evento responde 200 y registra en logs")
    void testCoreMockEndpoint() throws Exception {
        String jsonRequest = "{\"evento\": \"ACTUALIZACION\", \"polizaId\": 555}";

        mockMvc.perform(post("/api/v1/core-mock/evento")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REGISTRADO")))
                .andExpect(jsonPath("$.evento", is("ACTUALIZACION")))
                .andExpect(jsonPath("$.polizaId", is(555)));
    }

    @Test
    @DisplayName("Swagger OpenAPI: Endpoint /v3/api-docs responde 200 sin necesidad de header de auth")
    void testOpenApiDocsEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.info.title", containsString("Pólizas")));
    }

    @Test
    @DisplayName("Observabilidad: Endpoint /actuator/health responde 200 y status UP")
    void testActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Observabilidad: Las respuestas HTTP incluyen header X-Correlation-Id para trazabilidad")
    void testCorrelationIdHeaderPresente() throws Exception {
        mockMvc.perform(get("/api/v1/polizas")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
