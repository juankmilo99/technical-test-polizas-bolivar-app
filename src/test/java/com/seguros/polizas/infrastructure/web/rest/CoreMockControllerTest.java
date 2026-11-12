package com.seguros.polizas.infrastructure.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.infrastructure.config.ApiKeyFilter;
import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoreMockController.class)
@Import({ApiKeyFilter.class, GlobalExceptionHandler.class})
@DisplayName("Web Layer Unit Tests: CoreMockController")
class CoreMockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    @Test
    @DisplayName("POST /api/v1/core-mock/event with valid payload returns 200 and RECEIVED status")
    void testReceiveEventSuccess() throws Exception {
        CoreEventRequest req = new CoreEventRequest("POLICY_UPDATE", 555L);

        mockMvc.perform(post("/api/v1/core-mock/event")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andExpect(jsonPath("$.event", is("POLICY_UPDATE")))
                .andExpect(jsonPath("$.policyId", is(555)));
    }

    @Test
    @DisplayName("POST /api/v1/core-mock/event with missing fields returns 400 Bad Request")
    void testReceiveEventInvalidReturns400() throws Exception {
        CoreEventRequest req = new CoreEventRequest(null, null);

        mockMvc.perform(post("/api/v1/core-mock/event")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
