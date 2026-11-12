package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.CancelarPolizaCommand;
import com.seguros.polizas.application.command.RenovarPolizaCommand;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolizaCommandServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PolizaCommandService polizaCommandService;

    private Poliza polizaVigente;
    private Poliza polizaCancelada;

    @BeforeEach
    void setUp() {
        polizaVigente = new Poliza(
                "POL-TEST-001",
                TipoPoliza.INDIVIDUAL,
                EstadoPoliza.VIGENTE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                12,
                BigDecimal.valueOf(1000000.00),
                "Tomador Test",
                "Asegurado Test",
                "Beneficiario Test"
        );
        polizaVigente.setId(100L);

        Riesgo riesgo = new Riesgo(
                polizaVigente,
                EstadoRiesgo.ACTIVO,
                "Calle 123",
                "Bogota",
                "Asegurado Test",
                BigDecimal.valueOf(1000000.00)
        );
        polizaVigente.addRiesgo(riesgo);

        polizaCancelada = new Poliza(
                "POL-TEST-002",
                TipoPoliza.INDIVIDUAL,
                EstadoPoliza.CANCELADA,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                12,
                BigDecimal.valueOf(1000000.00),
                "Tomador Test",
                "Asegurado Test",
                "Beneficiario Test"
        );
        polizaCancelada.setId(200L);
    }

    @Test
    @DisplayName("Renovar póliza vigente incrementa canon y prima por IPC, cambia estado a RENOVADA y publica evento")
    void testRenovarPolizaVigenteExitosamente() {
        when(polizaRepository.findById(100L)).thenReturn(Optional.of(polizaVigente));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(i -> i.getArgument(0));

        Poliza resultado = polizaCommandService.renovar(new RenovarPolizaCommand(100L, BigDecimal.valueOf(5.0)));

        assertNotNull(resultado);
        assertEquals(EstadoPoliza.RENOVADA, resultado.getEstado());
        assertEquals(new BigDecimal("1050000.00"), resultado.getCanonMensual());
        assertEquals(new BigDecimal("12600000.00"), resultado.getPrimaTotal());

        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("No se puede renovar una póliza cancelada")
    void testRenovarPolizaCanceladaLanzaExcepcion() {
        when(polizaRepository.findById(200L)).thenReturn(Optional.of(polizaCancelada));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                polizaCommandService.renovar(new RenovarPolizaCommand(200L, BigDecimal.valueOf(5.0))));

        assertTrue(ex.getMessage().contains("No se puede renovar una poliza en estado CANCELADA"));
        verify(polizaRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Cancelar póliza cancela la póliza y todos sus riesgos en cascada y publica evento")
    void testCancelarPolizaCancelaTodosLosRiesgos() {
        when(polizaRepository.findById(100L)).thenReturn(Optional.of(polizaVigente));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(i -> i.getArgument(0));

        Poliza resultado = polizaCommandService.cancelar(new CancelarPolizaCommand(100L));

        assertEquals(EstadoPoliza.CANCELADA, resultado.getEstado());
        assertEquals(1, resultado.getRiesgos().size());
        assertEquals(EstadoRiesgo.CANCELADO, resultado.getRiesgos().get(0).getEstado());

        verify(eventPublisher, times(1)).publish(any());
    }
}
