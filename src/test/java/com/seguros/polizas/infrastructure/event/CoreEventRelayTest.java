package com.seguros.polizas.infrastructure.event;

import com.seguros.polizas.domain.event.PolicyCancelledEvent;
import com.seguros.polizas.domain.event.PolicyRenewedEvent;
import com.seguros.polizas.domain.event.RiskAddedEvent;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Domain Event Consumer Tests: CoreEventRelay")
class CoreEventRelayTest {

    @Mock
    private CoreIntegrationPort coreIntegrationPort;

    @InjectMocks
    private CoreEventRelay coreEventRelay;

    @Test
    @DisplayName("Relays policy renewed event to CORE with eventType and aggregateId")
    void testRelaysPolicyRenewedEvent() {
        PolicyRenewedEvent event = new PolicyRenewedEvent(10L, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1050000), BigDecimal.valueOf(5));

        coreEventRelay.onDomainEvent(event);

        verify(coreIntegrationPort, times(1)).notifyEvent("POLICY_RENEWED", 10L);
    }

    @Test
    @DisplayName("Relays policy cancelled event to CORE with eventType and aggregateId")
    void testRelaysPolicyCancelledEvent() {
        PolicyCancelledEvent event = new PolicyCancelledEvent(20L);

        coreEventRelay.onDomainEvent(event);

        verify(coreIntegrationPort, times(1)).notifyEvent("POLICY_CANCELLED", 20L);
    }

    @Test
    @DisplayName("Relays risk added event to CORE preserving policy aggregateId")
    void testRelaysRiskAddedEvent() {
        RiskAddedEvent event = new RiskAddedEvent(30L, 99L);

        coreEventRelay.onDomainEvent(event);

        verify(coreIntegrationPort, times(1)).notifyEvent("RISK_ADDED", 30L);
    }
}
