package com.seguros.polizas.application.port.in;

import com.seguros.polizas.application.command.AddRiskCommand;
import com.seguros.polizas.application.command.CancelRiskCommand;
import com.seguros.polizas.domain.model.Risk;

/**
 * Inbound port (CQRS - Command side) for Risk mutations.
 */
public interface RiskCommandHandler {

    Risk addRisk(AddRiskCommand command);

    Risk cancelRisk(CancelRiskCommand command);
}

