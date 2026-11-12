package com.seguros.polizas.application.command;

/**
 * Command to cancel an individual risk within a policy.
 */
public record CancelRiskCommand(
        Long riskId
) {
}

