package com.seguros.polizas.application.service;

import com.seguros.polizas.application.port.in.PolicyQuery;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.domain.port.out.RiskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of inbound port {@link PolicyQuery} (CQRS - Query side).
 */
@Service
@Transactional(readOnly = true)
public class PolicyQueryService implements PolicyQuery {

    private final PolicyRepository policyRepository;
    private final RiskRepository riskRepository;

    public PolicyQueryService(PolicyRepository policyRepository,
                              RiskRepository riskRepository) {
        this.policyRepository = policyRepository;
        this.riskRepository = riskRepository;
    }

    @Override
    public List<Policy> list(PolicyType type, PolicyStatus status) {
        if (type != null && status != null) {
            return policyRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            return policyRepository.findByType(type);
        } else if (status != null) {
            return policyRepository.findByStatus(status);
        }
        return policyRepository.findAll();
    }

    @Override
    public Policy getById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }

    @Override
    public List<Risk> listRisks(Long policyId) {
        if (!policyRepository.findById(policyId).isPresent()) {
            throw new PolicyNotFoundException(policyId);
        }
        return riskRepository.findByPolicyId(policyId);
    }
}
