package com.seguros.polizas.application.command;

/**
 * Comando (write model) para cancelar un riesgo individual.
 */
public record CancelarRiesgoCommand(
        Long riesgoId
) {
}
