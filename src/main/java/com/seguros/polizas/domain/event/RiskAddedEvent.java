package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Domain event emitted when a new risk is added to a collective policy.
 */
public record RiskAddedEvent(Long policyId, Long riskId, Instant occurredOn) implements DomainEvent {

    public RiskAddedEvent(Long policyId, Long riskId) {
        this(policyId, riskId, Instant.now());
    }

    @Override
    public String eventType() {
        return "RISK_ADDED";
    }

    @Override
    public Long aggregateId() {
        return policyId;
    }
}
