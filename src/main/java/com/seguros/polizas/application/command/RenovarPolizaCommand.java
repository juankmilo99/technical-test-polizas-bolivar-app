package com.seguros.polizas.application.command;

import java.math.BigDecimal;

/**
 * Comando (write model) para renovar una póliza aplicando IPC.
 */
public record RenovarPolizaCommand(
        Long polizaId,
        BigDecimal ipc
) {
}
