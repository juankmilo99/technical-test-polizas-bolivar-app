package com.seguros.polizas.infrastructure.web.rest;

import com.seguros.polizas.application.command.AddRiskCommand;
import com.seguros.polizas.application.command.CancelPolicyCommand;
import com.seguros.polizas.application.command.CreatePolicyCommand;
import com.seguros.polizas.application.command.RenewPolicyCommand;
import com.seguros.polizas.application.port.in.PolicyCommandHandler;
import com.seguros.polizas.application.port.in.PolicyQuery;
import com.seguros.polizas.application.port.in.RiskCommandHandler;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.infrastructure.web.rest.dto.PolicyRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.PolicyResponse;
import com.seguros.polizas.infrastructure.web.rest.dto.RenewalRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiskRequest;
import com.seguros.polizas.infrastructure.web.rest.dto.RiskResponse;
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
 * Inbound REST controller adapter for Policy management operations.
 */
@RestController
@RequestMapping({"/api/v1/policies", "/policies", "/api/v1/polizas", "/polizas"})
@Tag(name = "1. Policies", description = "Operations for rental policy management, renewals and cancellations")
public class PolicyController {

    private final PolicyCommandHandler policyCommandHandler;
    private final RiskCommandHandler riskCommandHandler;
    private final PolicyQuery policyQuery;

    public PolicyController(PolicyCommandHandler policyCommandHandler,
                            RiskCommandHandler riskCommandHandler,
                            PolicyQuery policyQuery) {
        this.policyCommandHandler = policyCommandHandler;
        this.riskCommandHandler = riskCommandHandler;
        this.policyQuery = policyQuery;
    }

    @Operation(summary = "List policies", description = "Retrieve all policies with optional filtering by type and status")
    @GetMapping
    public ResponseEntity<List<PolicyResponse>> list(
            @Parameter(description = "Policy type") @RequestParam(required = false) PolicyType type,
            @Parameter(description = "Legacy type param") @RequestParam(required = false, name = "tipo") PolicyType legacyType,
            @Parameter(description = "Policy status") @RequestParam(required = false) PolicyStatus status,
            @Parameter(description = "Legacy status param") @RequestParam(required = false, name = "estado") PolicyStatus legacyStatus) {

        PolicyType resolvedType = (type != null) ? type : legacyType;
        PolicyStatus resolvedStatus = (status != null) ? status : legacyStatus;

        List<PolicyResponse> result = policyQuery.list(resolvedType, resolvedStatus).stream()
                .map(PolicyResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get policy by ID", description = "Retrieve detailed information of a specific policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy found"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getById(@PathVariable Long id) {
        Policy policy = policyQuery.getById(id);
        return ResponseEntity.ok(PolicyResponse.fromEntity(policy));
    }

    @Operation(summary = "List risks of a policy", description = "Retrieve all property risks associated with the policy")
    @GetMapping({"/{id}/risks", "/{id}/riesgos"})
    public ResponseEntity<List<RiskResponse>> listRisks(@PathVariable Long id) {
        List<RiskResponse> risks = policyQuery.listRisks(id).stream()
                .map(RiskResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(risks);
    }

    @Operation(summary = "Create initial policy", description = "Registers a new policy and optionally creates its initial risk")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error in request payload")
    })
    @PostMapping
    public ResponseEntity<PolicyResponse> create(@Valid @RequestBody PolicyRequest request) {
        CreatePolicyCommand.InitialRisk initialRisk = null;
        if (request.getInitialRisk() != null) {
            initialRisk = new CreatePolicyCommand.InitialRisk(
                    request.getInitialRisk().getPropertyAddress(),
                    request.getInitialRisk().getCity(),
                    request.getInitialRisk().getInsuredName(),
                    request.getInitialRisk().getInsuredAmount()
            );
        }

        CreatePolicyCommand command = new CreatePolicyCommand(
                request.getPolicyNumber(),
                request.getType(),
                request.getStartDate(),
                request.getCoverageMonths(),
                request.getMonthlyRent(),
                request.getPolicyHolder(),
                request.getInsured(),
                request.getBeneficiary(),
                initialRisk
        );

        Policy created = policyCommandHandler.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PolicyResponse.fromEntity(created));
    }

    @Operation(summary = "Renew policy with IPC", description = "Increases monthly rent based on IPC formula and shifts validity forward")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy successfully renewed"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation (e.g. policy is CANCELLED)")
    })
    @PostMapping({"/{id}/renew", "/{id}/renovar"})
    public ResponseEntity<PolicyResponse> renew(
            @PathVariable Long id,
            @Valid @RequestBody RenewalRequest request) {

        Policy renewed = policyCommandHandler.renew(new RenewPolicyCommand(id, request.getIpc()));
        return ResponseEntity.ok(PolicyResponse.fromEntity(renewed));
    }

    @Operation(summary = "Cancel policy", description = "Cancels the policy and cascades the cancellation to all its risks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy and all risks successfully cancelled"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    @PostMapping({"/{id}/cancel", "/{id}/cancelar"})
    public ResponseEntity<PolicyResponse> cancel(@PathVariable Long id) {
        Policy cancelled = policyCommandHandler.cancel(new CancelPolicyCommand(id));
        return ResponseEntity.ok(PolicyResponse.fromEntity(cancelled));
    }

    @Operation(summary = "Add risk to collective policy", description = "Adds a risk to a COLLECTIVE policy. Rejects with 422 if policy is INDIVIDUAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Risk successfully added"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    @PostMapping({"/{id}/risks", "/{id}/riesgos"})
    public ResponseEntity<RiskResponse> addRisk(
            @PathVariable Long id,
            @Valid @RequestBody RiskRequest request) {

        AddRiskCommand command = new AddRiskCommand(
                id,
                request.getPropertyAddress(),
                request.getCity(),
                request.getInsuredName(),
                request.getInsuredAmount()
        );

        Risk savedRisk = riskCommandHandler.addRisk(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(RiskResponse.fromEntity(savedRisk));
    }
}
