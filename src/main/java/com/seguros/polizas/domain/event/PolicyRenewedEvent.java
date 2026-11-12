package com.seguros.polizas.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain event emitted when a policy is renewed with IPC index.
 */
public record PolicyRenewedEvent(
        Long policyId,
        BigDecimal previousRent,
        BigDecimal newRent,
        BigDecimal appliedIpc,
        Instant occurredOn
) implements DomainEvent {

    public PolicyRenewedEvent(Long policyId, BigDecimal previousRent, BigDecimal newRent, BigDecimal appliedIpc) {
        this(policyId, previousRent, newRent, appliedIpc, Instant.now());
    }

    @Override
    public String eventType() {
        return "POLICY_RENEWED";
    }

    @Override
    public Long aggregateId() {
        return policyId;
    }
}
