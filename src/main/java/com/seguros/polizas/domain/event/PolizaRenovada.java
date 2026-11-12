package com.seguros.polizas.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento publicado cuando una póliza es renovada aplicando IPC.
 */
public record PolizaRenovada(
        Long aggregateId,
        BigDecimal canonAnterior,
        BigDecimal canonNuevo,
        BigDecimal ipc,
        Instant occurredOn
) implements DomainEvent {

    public PolizaRenovada(Long aggregateId, BigDecimal canonAnterior, BigDecimal canonNuevo, BigDecimal ipc) {
        this(aggregateId, canonAnterior, canonNuevo, ipc, Instant.now());
    }

    @Override
    public String eventType() {
        return "RENOVACION";
    }
}
