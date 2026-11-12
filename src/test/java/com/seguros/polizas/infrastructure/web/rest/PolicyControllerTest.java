package com.seguros.polizas.infrastructure.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.application.port.in.PolicyCommandHandler;
import com.seguros.polizas.application.port.in.PolicyQuery;
import com.seguros.polizas.application.port.in.RiskCommandHandler;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.infrastructure.config.ApiKeyFilter;
import com.seguros.polizas.infrastructure.web.rest.dto.PolicyRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RenewalRequest;
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

@WebMvcTest(PolicyController.class)
@Import({ApiKeyFilter.class, GlobalExceptionHandler.class})
@DisplayName("Web Layer Unit Tests: PolicyController")
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyCommandHandler policyCommandHandler;

    @MockBean
    private RiskCommandHandler riskCommandHandler;

    @MockBean
    private PolicyQuery policyQuery;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    private Policy createPolicyFixture(Long id, PolicyType type, PolicyStatus status) {
        Policy p = new Policy(
                "POL-" + id, type, status,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 12,
                BigDecimal.valueOf(1500000.00), "PolicyHolder", "Insured", "Beneficiary");
        p.setId(id);
        return p;
    }

    @Test
    @DisplayName("GET /api/v1/policies with API Key returns 200 and list")
    void testListPoliciesReturns200() throws Exception {
        when(policyQuery.list(eq(PolicyType.INDIVIDUAL), eq(PolicyStatus.ACTIVE)))
                .thenReturn(List.of(createPolicyFixture(1L, PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/policies")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("type", "INDIVIDUAL")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].policyNumber", is("POL-1")));
    }

    @Test
    @DisplayName("GET /api/v1/policies without API Key returns 401 Unauthorized")
    void testListPoliciesWithoutApiKeyReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/policies/{id} returns 404 when not found")
    void testGetByIdNotFoundReturns404() throws Exception {
        when(policyQuery.getById(999L)).thenThrow(new PolicyNotFoundException(999L));

        mockMvc.perform(get("/api/v1/policies/999")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/v1/policies with invalid payload returns 400 Bad Request")
    void testCreateInvalidPayloadReturns400() throws Exception {
        PolicyRequest invalid = new PolicyRequest(); // Missing mandatory fields

        mockMvc.perform(post("/api/v1/policies")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_FAILED")));
    }

    @Test
    @DisplayName("POST /api/v1/policies/{id}/renew returns 200 on success")
    void testRenewPolicySuccessReturns200() throws Exception {
        Policy renewed = createPolicyFixture(1L, PolicyType.INDIVIDUAL, PolicyStatus.RENEWED);
        when(policyCommandHandler.renew(any())).thenReturn(renewed);

        RenewalRequest req = new RenewalRequest(new BigDecimal("5.10"));

        mockMvc.perform(post("/api/v1/policies/1/renew")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RENEWED")));
    }

    @Test
    @DisplayName("POST /api/v1/policies/{id}/renew throws BusinessRuleException returns 422")
    void testRenewCancelledPolicyReturns422() throws Exception {
        when(policyCommandHandler.renew(any()))
                .thenThrow(new BusinessRuleException("Cannot renew a policy in CANCELLED status."));

        RenewalRequest req = new RenewalRequest(new BigDecimal("5.10"));

        mockMvc.perform(post("/api/v1/policies/3/renew")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_VIOLATION")));
    }
}
