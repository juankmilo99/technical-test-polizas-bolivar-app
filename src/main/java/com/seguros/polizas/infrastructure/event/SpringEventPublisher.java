package com.seguros.polizas.infrastructure.event;

import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador (outbound) que publica eventos de dominio mediante el
 * {@code ApplicationEventPublisher} de Spring. Los consumidores (listeners)
 * deciden si reenviar el evento al CORE legado, notificar por email, etc.
 */
@Component
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
