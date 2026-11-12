package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.command.CancelarRiesgoCommand;
import com.seguros.polizas.application.port.in.RiesgoCommandHandler;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.infrastructure.web.rest.dto.RiesgoResponse;
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
 * Adapter de entrada (REST) para operaciones sobre riesgos.
 */
@RestController
@RequestMapping("/api/v1/riesgos")
@Tag(name = "2. Riesgos", description = "Operaciones sobre riesgos asegurables individuales")
public class RiesgoController {

    private final RiesgoCommandHandler riesgoCommandHandler;

    public RiesgoController(RiesgoCommandHandler riesgoCommandHandler) {
        this.riesgoCommandHandler = riesgoCommandHandler;
    }

    @Operation(summary = "Cancelar riesgo individual", description = "Cancela un riesgo individual cambiando su estado a CANCELADO y notifica al CORE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Riesgo cancelado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Riesgo no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<RiesgoResponse> cancelar(@PathVariable Long id) {
        Riesgo riesgo = riesgoCommandHandler.cancelarRiesgo(new CancelarRiesgoCommand(id));
        return ResponseEntity.ok(RiesgoResponse.fromEntity(riesgo));
    }
}
