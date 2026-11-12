package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Evento publicado cuando un riesgo individual es cancelado.
 */
public record RiesgoCancelado(
        Long aggregateId,
        Long riesgoId,
        Instant occurredOn
) implements DomainEvent {

    public RiesgoCancelado(Long aggregateId, Long riesgoId) {
        this(aggregateId, riesgoId, Instant.now());
    }

    @Override
    public String eventType() {
        return "CANCELACION_RIESGO";
    }
}
