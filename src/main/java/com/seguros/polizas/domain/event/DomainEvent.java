package com.seguros.polizas.domain.event;

import java.time.Instant;

/**
 * Evento de dominio: representación inmutable de un hecho relevante que
 * ha ocurrido en el agregado. Es la unidad de mensajería de la
 * Arquitectura Orientada a Eventos (EDA).
 */
public interface DomainEvent {

    /** Identificador del agregado origen del evento. */
    Long aggregateId();

    /** Tipo descriptivo del evento (ej. RENOVACION, CANCELACION_POLIZA). */
    String eventType();

    /** Momento en que se produjo el evento. */
    Instant occurredOn();
}
