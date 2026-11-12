package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter de entrada (REST) que simula el CORE asegurador (WebLogic).
 * Solo registra en logs los eventos entrantes.
 */
@RestController
@RequestMapping("/api/v1/core-mock")
@Tag(name = "3. Mock CORE WebLogic", description = "Servicio agnóstico de simulación para integración con el CORE asegurador transaccional")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @Operation(summary = "Recepción de eventos CORE", description = "Endpoint mock externo requerido para registrar en logs los intentos de envío de operaciones hacia el CORE legado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento recibido y registrado en log"),
            @ApiResponse(responseCode = "400", description = "Payload de evento inválido")
    })
    @PostMapping("/evento")
    public ResponseEntity<Map<String, Object>> recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        log.info("================================================================================");
        log.info(">>> [CORE-MOCK WEBLOGIC] REGISTRO DE EVENTO ENTRANTE AL SISTEMA TRANSACCIONAL CORE");
        log.info(">>> EVENTO   : {}", request.getEvento());
        log.info(">>> POLIZA_ID: {}", request.getPolizaId());
        log.info(">>> TIMESTAMP: {}", LocalDateTime.now());
        log.info("================================================================================");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "REGISTRADO");
        response.put("mensaje", "La operacion se intento enviar y fue registrada exitosamente en el CORE legado.");
        response.put("evento", request.getEvento());
        response.put("polizaId", request.getPolizaId());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
