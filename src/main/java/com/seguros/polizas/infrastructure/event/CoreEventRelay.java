package com.seguros.polizas.infrastructure.event;

import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Consumidor interno de eventos de dominio.
 *
 * <p>Representa el lado "event-driven" de la arquitectura: los eventos que
 * publica el dominio (tras confirmarse la transacción) se consumen aquí para
 * orquestar efectos secundarios. Actualmente reenvía al CORE legado a través
 * del ACL; es el punto natural para añadir notificaciones, auditoría o
 * proyecciones de lectura (CQRS read model).</p>
 */
@Component
public class CoreEventRelay {

    private final CoreIntegrationPort coreIntegrationPort;

    public CoreEventRelay(CoreIntegrationPort coreIntegrationPort) {
        this.coreIntegrationPort = coreIntegrationPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        coreIntegrationPort.notificarEvento(event.eventType(), event.aggregateId());
    }
}
