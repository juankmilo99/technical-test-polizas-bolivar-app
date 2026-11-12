package com.seguros.polizas.infrastructure.event;

import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Internal domain event consumer.
 *
 * <p>Implements Event-Driven Architecture: domain events emitted after a successful
 * database commit are consumed here to trigger asynchronous and integration side-effects.</p>
 */
@Component
public class CoreEventRelay {

    private final CoreIntegrationPort coreIntegrationPort;

    public CoreEventRelay(CoreIntegrationPort coreIntegrationPort) {
        this.coreIntegrationPort = coreIntegrationPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        coreIntegrationPort.notifyEvent(event.eventType(), event.aggregateId());
    }
}
