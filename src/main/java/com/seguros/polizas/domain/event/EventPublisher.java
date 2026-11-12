package com.seguros.polizas.domain.event;

/**
 * Puerto de salida (outbound) para publicar eventos de dominio.
 * Implementado en la capa de infraestructura mediante el
 * {@code ApplicationEventPublisher} de Spring y/o el reenvío al CORE legado.
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}
