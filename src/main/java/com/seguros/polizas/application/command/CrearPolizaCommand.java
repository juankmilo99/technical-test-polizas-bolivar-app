package com.seguros.polizas.application.command;

import com.seguros.polizas.domain.enums.TipoPoliza;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando (write model) para registrar una nueva póliza.
 */
public record CrearPolizaCommand(
        String numeroPoliza,
        TipoPoliza tipo,
        LocalDate fechaInicio,
        Integer mesesVigencia,
        BigDecimal canonMensual,
        String tomador,
        String asegurado,
        String beneficiario
) {
}
