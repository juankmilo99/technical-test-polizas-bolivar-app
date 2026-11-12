package com.seguros.polizas.infrastructure.event;

import com.seguros.polizas.domain.event.PolizaCancelada;
import com.seguros.polizas.domain.event.PolizaRenovada;
import com.seguros.polizas.domain.event.RiesgoAgregado;
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
@DisplayName("Pruebas del consumidor de eventos de dominio: CoreEventRelay")
class CoreEventRelayTest {

    @Mock
    private CoreIntegrationPort coreIntegrationPort;

    @InjectMocks
    private CoreEventRelay coreEventRelay;

    @Test
    @DisplayName("Reenvía un evento de renovación al CORE con su tipo y aggregateId")
    void testReenviaPolizaRenovada() {
        PolizaRenovada evento = new PolizaRenovada(10L, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1050000), BigDecimal.valueOf(5));

        coreEventRelay.onDomainEvent(evento);

        verify(coreIntegrationPort, times(1)).notificarEvento("RENOVACION", 10L);
    }

    @Test
    @DisplayName("Reenvía un evento de cancelación al CORE con su tipo y aggregateId")
    void testReenviaPolizaCancelada() {
        PolizaCancelada evento = new PolizaCancelada(20L);

        coreEventRelay.onDomainEvent(evento);

        verify(coreIntegrationPort, times(1)).notificarEvento("CANCELACION_POLIZA", 20L);
    }

    @Test
    @DisplayName("Reenvía un evento de inclusión de riesgo al CORE conservando el aggregateId de la póliza")
    void testReenviaRiesgoAgregado() {
        RiesgoAgregado evento = new RiesgoAgregado(30L, 55L);

        coreEventRelay.onDomainEvent(evento);

        verify(coreIntegrationPort, times(1)).notificarEvento("INCLUSION_RIESGO", 30L);
    }
}
