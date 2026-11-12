package com.seguros.polizas.domain.port.out;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.model.Policy;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for Policy persistence.
 */
public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(Long id);

    List<Policy> findAll();

    List<Policy> findByTypeAndStatus(PolicyType type, PolicyStatus status);

    List<Policy> findByType(PolicyType type);

    List<Policy> findByStatus(PolicyStatus status);

    boolean existsByPolicyNumber(String policyNumber);

    void deleteById(Long id);
}

