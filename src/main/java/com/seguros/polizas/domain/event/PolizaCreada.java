package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Evento publicado cuando se registra una nueva póliza.
 */
public record PolizaCreada(
        Long aggregateId,
        String numeroPoliza,
        Instant occurredOn
) implements DomainEvent {

    public PolizaCreada(Long aggregateId, String numeroPoliza) {
        this(aggregateId, numeroPoliza, Instant.now());
    }

    @Override
    public String eventType() {
        return "CREACION_POLIZA";
    }
}
