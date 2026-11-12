package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.command.CancelRiskCommand;
import com.seguros.polizas.application.port.in.RiskCommandHandler;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.infrastructure.web.rest.dto.RiskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller adapter for direct Risk operations.
 */
@RestController
@RequestMapping({"/api/v1/risks", "/risks", "/api/v1/riesgos", "/riesgos"})
@Tag(name = "2. Risks", description = "Operations on individual property risks")
public class RiskController {

    private final RiskCommandHandler riskCommandHandler;

    public RiskController(RiskCommandHandler riskCommandHandler) {
        this.riskCommandHandler = riskCommandHandler;
    }

    @Operation(summary = "Cancel individual risk", description = "Cancels a specific risk without cancelling the parent policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risk successfully cancelled"),
            @ApiResponse(responseCode = "404", description = "Risk not found")
    })
    @PostMapping({"/{id}/cancel", "/{id}/cancelar"})
    public ResponseEntity<RiskResponse> cancelRisk(@PathVariable Long id) {
        Risk cancelled = riskCommandHandler.cancelRisk(new CancelRiskCommand(id));
        return ResponseEntity.ok(RiskResponse.fromEntity(cancelled));
    }
}

