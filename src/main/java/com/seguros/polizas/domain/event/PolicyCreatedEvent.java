package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Domain event emitted when a new policy is created.
 */
public record PolicyCreatedEvent(Long policyId, String policyNumber, Instant occurredOn) implements DomainEvent {

    public PolicyCreatedEvent(Long policyId, String policyNumber) {
        this(policyId, policyNumber, Instant.now());
    }

    @Override
    public String eventType() {
        return "POLICY_CREATED";
    }

    @Override
    public Long aggregateId() {
        return policyId;
    }
}
