package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Evento publicado cuando se agrega un riesgo a una póliza colectiva.
 */
public record RiesgoAgregado(
        Long aggregateId,
        Long riesgoId,
        Instant occurredOn
) implements DomainEvent {

    public RiesgoAgregado(Long aggregateId, Long riesgoId) {
        this(aggregateId, riesgoId, Instant.now());
    }

    @Override
    public String eventType() {
        return "INCLUSION_RIESGO";
    }
}
