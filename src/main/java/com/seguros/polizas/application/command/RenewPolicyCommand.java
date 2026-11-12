package com.seguros.polizas.application.command;

import java.math.BigDecimal;

/**
 * Command to renew an existing policy using the IPC adjustment index.
 */
public record RenewPolicyCommand(
        Long policyId,
        BigDecimal ipc
) {
}
