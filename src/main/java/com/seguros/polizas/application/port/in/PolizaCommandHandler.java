package com.seguros.polizas.application.port.in;

import com.seguros.polizas.application.command.CancelarPolizaCommand;
import com.seguros.polizas.application.command.CrearPolizaCommand;
import com.seguros.polizas.application.command.RenovarPolizaCommand;
import com.seguros.polizas.domain.model.Poliza;

/**
 * Puerto de entrada (CQRS - Command side) para operaciones de mutación
 * sobre el agregado Poliza.
 */
public interface PolizaCommandHandler {

    Poliza crear(CrearPolizaCommand command);

    Poliza renovar(RenovarPolizaCommand command);

    Poliza cancelar(CancelarPolizaCommand command);
}
