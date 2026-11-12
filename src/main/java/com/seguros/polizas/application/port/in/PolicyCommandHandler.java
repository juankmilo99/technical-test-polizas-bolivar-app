package com.seguros.polizas.application.port.in;

import com.seguros.polizas.application.command.CancelPolicyCommand;
import com.seguros.polizas.application.command.CreatePolicyCommand;
import com.seguros.polizas.application.command.RenewPolicyCommand;
import com.seguros.polizas.domain.model.Policy;

/**
 * Inbound port (CQRS - Command side) for Policy mutations.
 */
public interface PolicyCommandHandler {

    Policy create(CreatePolicyCommand command);

    Policy renew(RenewPolicyCommand command);

    Policy cancel(CancelPolicyCommand command);
}
