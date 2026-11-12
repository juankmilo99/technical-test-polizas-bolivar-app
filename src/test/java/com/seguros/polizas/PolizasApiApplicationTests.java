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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Spring Boot Integration Tests: Policies Application Suite")
class PolizasApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreIntegrationPort coreService;

    @Test
    @DisplayName("Security: Request without x-api-key returns 401 Unauthorized")
    void testAccessWithoutApiKeyReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Security: Request with valid x-api-key returns 200 OK")
    void testAccessWithValidApiKeyReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/policies")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Endpoint 1: List policies with type and status filters")
    void testListPoliciesWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/policies")
                        .param("type", "INDIVIDUAL")
                        .param("status", "ACTIVE")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("INDIVIDUAL")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Endpoint 2: List risks of existing policy")
    void testListRisksOfPolicy() throws Exception {
        mockMvc.perform(get("/api/v1/policies/1/risks")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Endpoint 3: Renew active policy increases values and returns RENEWED status")
    void testRenewActivePolicy() throws Exception {
        String jsonRequest = "{\"ipc\": 5.10}";

        mockMvc.perform(post("/api/v1/policies/1/renew")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RENEWED")))
                .andExpect(jsonPath("$.monthlyRent", notNullValue()))
                .andExpect(jsonPath("$.totalPremium", notNullValue()));

        verify(coreService, atLeastOnce()).notifyEvent(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Business Rule: Cannot renew cancelled policy (returns 422)")
    void testRenewCancelledPolicyReturns422() throws Exception {
        String jsonRequest = "{\"ipc\": 5.10}";

        mockMvc.perform(post("/api/v1/policies/3/renew")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_VIOLATION")));
    }

    @Test
    @DisplayName("Business Rule: Adding risk to individual policy returns 422")
    void testAddRiskToIndividualPolicyReturns422() throws Exception {
        String jsonRequest = "{"
                + "\"propertyAddress\": \"Calle 50 # 10-20\","
                + "\"city\": \"Bogota\","
                + "\"insuredName\": \"New Tenant\","
                + "\"insuredAmount\": 1800000.00"
                + "}";

        mockMvc.perform(post("/api/v1/policies/1/risks")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_VIOLATION")));
    }

    @Test
    @DisplayName("Endpoint 5: Add risk to collective policy returns 201 Created")
    void testAddRiskToCollectivePolicyReturns201() throws Exception {
        String jsonRequest = "{"
                + "\"propertyAddress\": \"Avenida Siempre Viva 742\","
                + "\"city\": \"Bogota\","
                + "\"insuredName\": \"Company XYZ\","
                + "\"insuredAmount\": 3500000.00"
                + "}";

        mockMvc.perform(post("/api/v1/policies/2/risks")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(coreService, atLeastOnce()).notifyEvent(anyString(), eq(2L));
    }

    @Test
    @DisplayName("Mock CORE: POST /api/v1/core-mock/event responds 200 and logs event")
    void testCoreMockEndpoint() throws Exception {
        String jsonRequest = "{\"event\": \"POLICY_UPDATE\", \"policyId\": 555}";

        mockMvc.perform(post("/api/v1/core-mock/event")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andExpect(jsonPath("$.event", is("POLICY_UPDATE")))
                .andExpect(jsonPath("$.policyId", is(555)));
    }

    @Test
    @DisplayName("Swagger OpenAPI: Endpoint /v3/api-docs responds 200 without auth header")
    void testOpenApiDocsEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()));
    }

    @Test
    @DisplayName("Observability: Endpoint /actuator/health responds 200 and UP status")
    void testActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Observability: HTTP responses include X-Correlation-Id header")
    void testCorrelationIdHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/v1/policies")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
