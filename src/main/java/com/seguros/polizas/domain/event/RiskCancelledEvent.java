package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Domain event emitted when an individual risk is cancelled.
 */
public record RiskCancelledEvent(Long policyId, Long riskId, Instant occurredOn) implements DomainEvent {

    public RiskCancelledEvent(Long policyId, Long riskId) {
        this(policyId, riskId, Instant.now());
    }

    @Override
    public String eventType() {
        return "RISK_CANCELLED";
    }

    @Override
    public Long aggregateId() {
        return policyId;
    }
}
