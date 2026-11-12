package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.CancelPolicyCommand;
import com.seguros.polizas.application.command.CreatePolicyCommand;
import com.seguros.polizas.application.command.RenewPolicyCommand;
import com.seguros.polizas.application.port.in.PolicyCommandHandler;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.event.PolicyCreatedEvent;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.domain.port.out.RiskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implementation of inbound port {@link PolicyCommandHandler}.
 *
 * <p>Orchestrates write use cases (CQRS command side): loads the aggregate,
 * delegates state mutations to the domain, persists changes and publishes
 * emitted domain events through the {@link EventPublisher}.</p>
 */
@Service
@Transactional
public class PolicyCommandService implements PolicyCommandHandler {

    private final PolicyRepository policyRepository;
    private final RiskRepository riskRepository;
    private final EventPublisher eventPublisher;

    public PolicyCommandService(PolicyRepository policyRepository,
                                RiskRepository riskRepository,
                                EventPublisher eventPublisher) {
        this.policyRepository = policyRepository;
        this.riskRepository = riskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Policy create(CreatePolicyCommand command) {
        if (policyRepository.existsByPolicyNumber(command.policyNumber())) {
            throw new BusinessRuleException("A policy already exists with number: " + command.policyNumber());
        }

        LocalDate startDate = command.startDate();
        LocalDate endDate = startDate.plusMonths(command.coverageMonths()).minusDays(1);

        Policy policy = new Policy(
                command.policyNumber(),
                command.type(),
                PolicyStatus.ACTIVE,
                startDate,
                endDate,
                command.coverageMonths(),
                command.monthlyRent(),
                command.policyHolder(),
                command.insured(),
                command.beneficiary()
        );

        if (command.initialRisk() != null) {
            Risk initialRisk = new Risk(
                    policy,
                    RiskStatus.ACTIVE,
                    command.initialRisk().propertyAddress(),
                    command.initialRisk().city(),
                    command.initialRisk().insuredName(),
                    command.initialRisk().insuredAmount()
            );
            policy.addRisk(initialRisk);
        }

        Policy savedPolicy = policyRepository.save(policy);
        publish(new PolicyCreatedEvent(savedPolicy.getId(), savedPolicy.getPolicyNumber()));
        return savedPolicy;
    }

    @Override
    public Policy renew(RenewPolicyCommand command) {
        Policy policy = policyRepository.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot renew a policy in CANCELLED status.");
        }

        policy.renew(command.ipc());
        Policy savedPolicy = policyRepository.save(policy);
        publishDrained(policy);
        return savedPolicy;
    }

    @Override
    public Policy cancel(CancelPolicyCommand command) {
        Policy policy = policyRepository.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        policy.cancel();
        Policy savedPolicy = policyRepository.save(policy);
        publishDrained(policy);
        return savedPolicy;
    }

    private void publishDrained(Policy policy) {
        for (DomainEvent event : policy.drainEvents()) {
            publish(event);
        }
    }

    private void publish(DomainEvent event) {
        eventPublisher.publish(event);
    }
}

