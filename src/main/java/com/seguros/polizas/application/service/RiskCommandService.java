package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.AddRiskCommand;
import com.seguros.polizas.application.command.CancelRiskCommand;
import com.seguros.polizas.application.port.in.RiskCommandHandler;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.event.RiskAddedEvent;
import com.seguros.polizas.domain.event.RiskCancelledEvent;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.domain.port.out.RiskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of inbound port {@link RiskCommandHandler}.
 */
@Service
@Transactional
public class RiskCommandService implements RiskCommandHandler {

    private final PolicyRepository policyRepository;
    private final RiskRepository riskRepository;
    private final EventPublisher eventPublisher;

    public RiskCommandService(PolicyRepository policyRepository,
                              RiskRepository riskRepository,
                              EventPublisher eventPublisher) {
        this.policyRepository = policyRepository;
        this.riskRepository = riskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Risk addRisk(AddRiskCommand command) {
        Policy policy = policyRepository.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        if (policy.getType() == PolicyType.INDIVIDUAL) {
            throw new BusinessRuleException(
                    "Operation rejected: An individual policy can only have 1 risk. " +
                    "To manage multiple risks the policy must be COLLECTIVE."
            );
        }

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Operation rejected: Cannot add risks to a CANCELLED policy."
            );
        }

        Risk newRisk = new Risk(
                policy,
                RiskStatus.ACTIVE,
                command.propertyAddress(),
                command.city(),
                command.insuredName(),
                command.insuredAmount()
        );

        policy.addRisk(newRisk);
        Risk savedRisk = riskRepository.save(newRisk);
        eventPublisher.publish(new RiskAddedEvent(command.policyId(), savedRisk.getId()));
        return savedRisk;
    }

    @Override
    public Risk cancelRisk(CancelRiskCommand command) {
        Risk risk = riskRepository.findById(command.riskId())
                .orElseThrow(() -> new PolicyNotFoundException("Risk not found with ID: " + command.riskId()));

        risk.cancel();
        Risk savedRisk = riskRepository.save(risk);
        Long policyId = risk.getPolicy() != null ? risk.getPolicy().getId() : null;
        eventPublisher.publish(new RiskCancelledEvent(policyId, savedRisk.getId()));
        return savedRisk;
    }
}

