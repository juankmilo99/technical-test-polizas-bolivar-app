package com.seguros.polizas.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import com.seguros.polizas.infrastructure.web.rest.dto.PolicyRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RenewalRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiskRequest;
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
@DisplayName("End-to-End Integration Test: Complete Lifecycle of Policies and Risks")
class PolicyLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoreIntegrationPort coreService;

    private static final String API_KEY = "123456";

    @Test
    @DisplayName("Complete E2E Lifecycle: Collective Creation -> Add Multiple Risks -> Cancel Risk -> IPC Renewal -> Cascade Policy Cancel -> Invariants")
    void testCompleteLifecycleCollectivePolicy() throws Exception {
        // 1. Create a Collective Policy
        PolicyRequest createReq = new PolicyRequest();
        createReq.setPolicyNumber("POL-COL-E2E-999");
        createReq.setType(PolicyType.COLLECTIVE);
        createReq.setStartDate(LocalDate.of(2026, 1, 1));
        createReq.setCoverageMonths(12);
        createReq.setMonthlyRent(new BigDecimal("5000000.00"));
        createReq.setPolicyHolder("Inmobiliaria Central");
        createReq.setInsured("Multiple Tenants");
        createReq.setBeneficiary("Property Owners");

        MvcResult resultCreate = mockMvc.perform(post("/api/v1/policies")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.totalPremium", is(60000000.00)))
                .andReturn();

        JsonNode jsonPolicy = objectMapper.readTree(resultCreate.getResponse().getContentAsString());
        long policyId = jsonPolicy.get("id").asLong();

        // 2. Add Risk 1
        RiskRequest risk1 = new RiskRequest("Avenida Chile # 72-10", "Bogota", "Tenant Alpha", new BigDecimal("2500000.00"));
        MvcResult resR1 = mockMvc.perform(post("/api/v1/policies/" + policyId + "/risks")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(risk1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn();
        long r1Id = objectMapper.readTree(resR1.getResponse().getContentAsString()).get("id").asLong();

        // 3. Add Risk 2
        RiskRequest risk2 = new RiskRequest("Calle 72 # 10-34", "Bogota", "Tenant Beta", new BigDecimal("2500000.00"));
        mockMvc.perform(post("/api/v1/policies/" + policyId + "/risks")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(risk2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // 4. Query Risks of the Policy
        mockMvc.perform(get("/api/v1/policies/" + policyId + "/risks")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Cancel Risk 1 individually
        mockMvc.perform(post("/api/v1/risks/" + r1Id + "/cancel")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // 6. Renew the Collective Policy with 5.10% IPC
        RenewalRequest renReq = new RenewalRequest(new BigDecimal("5.10"));
        mockMvc.perform(post("/api/v1/policies/" + policyId + "/renew")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RENEWED")))
                // 5,000,000 * 1.051 = 5,255,000.00
                .andExpect(jsonPath("$.monthlyRent", is(5255000.00)))
                // 5,255,000 * 12 = 63,060,000.00
                .andExpect(jsonPath("$.totalPremium", is(63060000.00)));

        // 7. Cancel the Collective Policy in cascade
        mockMvc.perform(post("/api/v1/policies/" + policyId + "/cancel")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // 8. Verify all associated risks are now CANCELLED
        MvcResult resRisksEnd = mockMvc.perform(get("/api/v1/policies/" + policyId + "/risks")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode risksEndArr = objectMapper.readTree(resRisksEnd.getResponse().getContentAsString());
        for (JsonNode r : risksEndArr) {
            assertEquals("CANCELLED", r.get("status").asText());
        }

        // 9. Verify Invariant: Cannot renew a cancelled policy (returns 422)
        mockMvc.perform(post("/api/v1/policies/" + policyId + "/renew")
                        .header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }
}

