package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventRequest;
import io.swagger.v3.oas.annotations.Operation;
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
 * Mock endpoint simulating the legacy WebLogic CORE service.
 */
@RestController
@RequestMapping("/api/v1/core-mock")
@Tag(name = "3. CORE Mock", description = "Mock simulating legacy WebLogic insurance system event reception")
public class CoreMockController {

    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @Operation(summary = "Receive event mock", description = "Receives business events emitted by the policy management service")
    @PostMapping("/event")
    public ResponseEntity<Map<String, Object>> receiveEvent(@Valid @RequestBody CoreEventRequest request) {
        log.info("[CORE-MOCK] >> Event received from modern policy service: event='{}', policyId={}",
                request.getEvent(), request.getPolicyId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "RECEIVED");
        response.put("message", "Event successfully logged in CORE");
        response.put("event", request.getEvent());
        response.put("policyId", request.getPolicyId());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
