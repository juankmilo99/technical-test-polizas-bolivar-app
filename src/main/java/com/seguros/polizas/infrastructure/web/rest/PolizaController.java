package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.command.AgregarRiesgoCommand;
import com.seguros.polizas.application.command.CancelarPolizaCommand;
import com.seguros.polizas.application.command.CrearPolizaCommand;
import com.seguros.polizas.application.command.RenovarPolizaCommand;
import com.seguros.polizas.application.port.in.PolizaCommandHandler;
import com.seguros.polizas.application.port.in.PolizaQuery;
import com.seguros.polizas.application.port.in.RiesgoCommandHandler;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.infrastructure.web.rest.dto.PolizaRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.PolizaResponse;
import com.seguros.polizas.infrastructure.web.rest.dto.RenovacionRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiesgoRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiesgoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter de entrada (REST) para las operaciones de póliza y riesgo.
 *
 * <p>Se limita a traducir HTTP &lt;-&gt; casos de uso (puertos de entrada),
 * sin contener lógica de negocio.</p>
 */
@RestController
@RequestMapping("/api/v1/polizas")
@Tag(name = "1. Pólizas", description = "Operaciones principales para la administración, renovación y cancelación de pólizas de arrendamiento")
public class PolizaController {

    private final PolizaCommandHandler polizaCommandHandler;
    private final RiesgoCommandHandler riesgoCommandHandler;
    private final PolizaQuery polizaQuery;

    public PolizaController(PolizaCommandHandler polizaCommandHandler,
                            RiesgoCommandHandler riesgoCommandHandler,
                            PolizaQuery polizaQuery) {
        this.polizaCommandHandler = polizaCommandHandler;
        this.riesgoCommandHandler = riesgoCommandHandler;
        this.polizaQuery = polizaQuery;
    }

    @Operation(summary = "Listar pólizas", description = "Consulta todas las pólizas con opción de filtrar por 'tipo' y 'estado'")
    @GetMapping
    public ResponseEntity<List<PolizaResponse>> listar(
            @Parameter(description = "Tipo de póliza") @RequestParam(required = false) TipoPoliza tipo,
            @Parameter(description = "Estado de la póliza") @RequestParam(required = false) EstadoPoliza estado) {

        List<Poliza> polizas = polizaQuery.listar(tipo, estado);
        List<PolizaResponse> response = polizas.stream()
                .map(PolizaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar póliza por ID", description = "Retorna el detalle completo de una póliza incluyendo sus riesgos asociados")
    @GetMapping("/{id}")
    public ResponseEntity<PolizaResponse> obtenerPorId(@PathVariable Long id) {
        Poliza poliza = polizaQuery.obtenerPorId(id);
        return ResponseEntity.ok(PolizaResponse.fromEntity(poliza));
    }

    @Operation(summary = "Listar riesgos de una póliza", description = "Consulta los riesgos vinculados a una póliza específica")
    @GetMapping("/{id}/riesgos")
    public ResponseEntity<List<RiesgoResponse>> listarRiesgos(@PathVariable Long id) {
        List<Riesgo> riesgos = polizaQuery.listarRiesgos(id);
        List<RiesgoResponse> response = riesgos.stream()
                .map(RiesgoResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear póliza", description = "Registra una nueva póliza individual o colectiva en el sistema")
    @PostMapping
    public ResponseEntity<PolizaResponse> crear(@Valid @RequestBody PolizaRequest request) {
        CrearPolizaCommand command = new CrearPolizaCommand(
                request.getNumeroPoliza(),
                request.getTipo(),
                request.getFechaInicio(),
                request.getMesesVigencia(),
                request.getCanonMensual(),
                request.getTomador(),
                request.getAsegurado(),
                request.getBeneficiario()
        );
        Poliza poliza = polizaCommandHandler.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PolizaResponse.fromEntity(poliza));
    }

    @Operation(summary = "Renovar póliza", description = "Incrementa el canon y la prima por el IPC (+IPC%), actualiza el estado a RENOVADA y notifica al CORE.")
    @PostMapping("/{id}/renovar")
    public ResponseEntity<PolizaResponse> renovar(
            @PathVariable Long id,
            @Valid @RequestBody RenovacionRequest request) {

        RenovarPolizaCommand command = new RenovarPolizaCommand(id, request.getIpc());
        Poliza poliza = polizaCommandHandler.renovar(command);
        return ResponseEntity.ok(PolizaResponse.fromEntity(poliza));
    }

    @Operation(summary = "Cancelar póliza", description = "Cancela la póliza y en cascada pasa todos sus riesgos asociados a CANCELADO.")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PolizaResponse> cancelar(@PathVariable Long id) {
        Poliza poliza = polizaCommandHandler.cancelar(new CancelarPolizaCommand(id));
        return ResponseEntity.ok(PolizaResponse.fromEntity(poliza));
    }

    @Operation(summary = "Agregar riesgo a póliza colectiva", description = "Agrega un nuevo riesgo a una póliza existente. Solo permitido en pólizas COLECTIVAS.")
    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(
            @PathVariable Long id,
            @Valid @RequestBody RiesgoRequest request) {

        AgregarRiesgoCommand command = new AgregarRiesgoCommand(
                id,
                request.getDireccionInmueble(),
                request.getCiudad(),
                request.getNombreAsegurado(),
                request.getValorAsegurado()
        );
        Riesgo riesgo = riesgoCommandHandler.agregarRiesgo(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(RiesgoResponse.fromEntity(riesgo));
    }
}
