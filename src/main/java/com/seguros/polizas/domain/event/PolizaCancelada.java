package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Evento publicado cuando una póliza es cancelada (en cascada sobre sus riesgos).
 */
public record PolizaCancelada(
        Long aggregateId,
        Instant occurredOn
) implements DomainEvent {

    public PolizaCancelada(Long aggregateId) {
        this(aggregateId, Instant.now());
    }

    @Override
    public String eventType() {
        return "CANCELACION_POLIZA";
    }
}
