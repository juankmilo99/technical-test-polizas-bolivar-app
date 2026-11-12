package com.seguros.polizas.infrastructure.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.infrastructure.config.ApiKeyFilter;
import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventoRequest;
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
@DisplayName("Pruebas Unitarias de Capa Web: CoreMockController")
class CoreMockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_KEY_VALUE = "123456";

    @Test
    @DisplayName("POST /api/v1/core-mock/evento con payload válido retorna 200 y status REGISTRADO")
    void testRecibirEventoExitoso() throws Exception {
        CoreEventoRequest req = new CoreEventoRequest("ACTUALIZACION", 555L);

        mockMvc.perform(post("/api/v1/core-mock/evento")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REGISTRADO")))
                .andExpect(jsonPath("$.evento", is("ACTUALIZACION")))
                .andExpect(jsonPath("$.polizaId", is(555)));
    }

    @Test
    @DisplayName("POST /api/v1/core-mock/evento con campos nulos retorna 400 Bad Request")
    void testRecibirEventoInvalidoRetorna400() throws Exception {
        CoreEventoRequest reqInvalido = new CoreEventoRequest("", null);

        mockMvc.perform(post("/api/v1/core-mock/evento")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validacion de entrada fallida")));
    }
}
