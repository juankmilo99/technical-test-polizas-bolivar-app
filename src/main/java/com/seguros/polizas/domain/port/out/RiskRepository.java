package com.seguros.polizas.domain.port.out;

import com.seguros.polizas.domain.model.Risk;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for Risk persistence.
 */
public interface RiskRepository {

    Risk save(Risk risk);

    Optional<Risk> findById(Long id);

    List<Risk> findByPolicyId(Long policyId);
}

