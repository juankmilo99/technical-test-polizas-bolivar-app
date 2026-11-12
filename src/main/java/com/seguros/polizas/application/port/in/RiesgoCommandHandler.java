package com.seguros.polizas.application.port.in;

import com.seguros.polizas.application.command.AgregarRiesgoCommand;
import com.seguros.polizas.application.command.CancelarRiesgoCommand;
import com.seguros.polizas.domain.model.Riesgo;

/**
 * Puerto de entrada (CQRS - Command side) para operaciones de mutación
 * sobre la entidad Riesgo.
 */
public interface RiesgoCommandHandler {

    Riesgo agregarRiesgo(AgregarRiesgoCommand command);

    Riesgo cancelarRiesgo(CancelarRiesgoCommand command);
}
