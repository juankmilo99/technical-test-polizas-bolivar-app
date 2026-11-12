package com.seguros.polizas.application.port.in;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;

import java.util.List;

/**
 * Inbound port (CQRS - Query side) for Policy read operations.
 */
public interface PolicyQuery {

    List<Policy> list(PolicyType type, PolicyStatus status);

    Policy getById(Long id);

    List<Risk> listRisks(Long policyId);
}

