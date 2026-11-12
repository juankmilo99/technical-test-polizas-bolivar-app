package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Domain event emitted when a policy and its risks are cancelled.
 */
public record PolicyCancelledEvent(Long policyId, Instant occurredOn) implements DomainEvent {

    public PolicyCancelledEvent(Long policyId) {
        this(policyId, Instant.now());
    }

    @Override
    public String eventType() {
        return "POLICY_CANCELLED";
    }

    @Override
    public Long aggregateId() {
        return policyId;
    }
}
