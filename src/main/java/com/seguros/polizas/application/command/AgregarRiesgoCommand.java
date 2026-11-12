package com.seguros.polizas.application.command;

import java.math.BigDecimal;

/**
 * Comando (write model) para agregar un riesgo a una póliza colectiva.
 */
public record AgregarRiesgoCommand(
        Long polizaId,
        String direccionInmueble,
        String ciudad,
        String nombreAsegurado,
        BigDecimal valorAsegurado
) {
}
