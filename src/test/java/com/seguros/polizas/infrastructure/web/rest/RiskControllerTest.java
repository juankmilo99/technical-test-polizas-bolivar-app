package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.port.in.RiskCommandHandler;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Risk;
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

@WebMvcTest(RiskController.class)
@Import({ApiKeyFilter.class, GlobalExceptionHandler.class})
@DisplayName("Web Layer Unit Tests: RiskController")
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskCommandHandler riskCommandHandler;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    @Test
    @DisplayName("POST /api/v1/risks/{id}/cancel cancels existing risk and returns 200 OK")
    void testCancelRiskSuccess() throws Exception {
        Risk risk = new Risk(null, RiskStatus.CANCELLED, "Calle 100", "Bogota", "Tenant", BigDecimal.valueOf(1000));
        risk.setId(10L);

        when(riskCommandHandler.cancelRisk(any())).thenReturn(risk);

        mockMvc.perform(post("/api/v1/risks/10/cancel")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("POST /api/v1/risks/{id}/cancel throws PolicyNotFoundException returns 404")
    void testCancelRiskNotFoundReturns404() throws Exception {
        when(riskCommandHandler.cancelRisk(any()))
                .thenThrow(new PolicyNotFoundException("Risk not found with ID: 999"));

        mockMvc.perform(post("/api/v1/risks/999/cancel")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }
}
