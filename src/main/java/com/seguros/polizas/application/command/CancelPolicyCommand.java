package com.seguros.polizas.application.command;

/**
 * Command to cancel a policy and all its associated risks in cascade.
 */
public record CancelPolicyCommand(
        Long policyId
) {
}

