package com.seguros.polizas.application.command;

/**
 * Comando (write model) para cancelar una póliza.
 */
public record CancelarPolizaCommand(
        Long polizaId
) {
}
