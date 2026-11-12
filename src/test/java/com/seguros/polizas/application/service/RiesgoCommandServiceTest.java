package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.AgregarRiesgoCommand;
import com.seguros.polizas.application.command.CancelarRiesgoCommand;
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
class RiesgoCommandServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RiesgoCommandService riesgoCommandService;

    private Poliza polizaIndividual;
    private Poliza polizaColectiva;

    @BeforeEach
    void setUp() {
        polizaIndividual = new Poliza(
                "POL-IND-001", TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE,
                LocalDate.now(), LocalDate.now().plusMonths(12), 12,
                BigDecimal.valueOf(1500000), "Tomador", "Asegurado", "Beneficiario");
        polizaIndividual.setId(1L);

        polizaColectiva = new Poliza(
                "POL-COL-001", TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE,
                LocalDate.now(), LocalDate.now().plusMonths(12), 12,
                BigDecimal.valueOf(5000000), "Inmobiliaria", "Varios", "Propietarios");
        polizaColectiva.setId(2L);
    }

    @Test
    @DisplayName("Agregar riesgo a póliza colectiva es exitoso")
    void testAgregarRiesgoAPolizaColectivaExitoso() {
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(polizaColectiva));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(i -> {
            Riesgo r = i.getArgument(0);
            r.setId(99L);
            return r;
        });

        AgregarRiesgoCommand cmd = new AgregarRiesgoCommand(2L, "Calle 100", "Bogota", "Inquilino 1", BigDecimal.valueOf(2000000));
        Riesgo guardado = riesgoCommandService.agregarRiesgo(cmd);

        assertNotNull(guardado);
        assertEquals(EstadoRiesgo.ACTIVO, guardado.getEstado());
        assertEquals("Calle 100", guardado.getDireccionInmueble());

        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("Agregar riesgo a póliza individual está PROHIBIDO")
    void testAgregarRiesgoAPolizaIndividualLanzaExcepcion() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual));

        AgregarRiesgoCommand cmd = new AgregarRiesgoCommand(1L, "Carrera 7", "Bogota", "Inquilino 2", BigDecimal.valueOf(1500000));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                riesgoCommandService.agregarRiesgo(cmd));

        assertTrue(ex.getMessage().contains("Una poliza individual solo puede tener 1 riesgo"));
        verify(riesgoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Cancelar riesgo individual cambia estado a CANCELADO y publica evento")
    void testCancelarRiesgoIndividual() {
        Riesgo riesgo = new Riesgo(polizaColectiva, EstadoRiesgo.ACTIVO, "Dir 1", "Cali", "Asegurado", BigDecimal.valueOf(1000));
        riesgo.setId(50L);

        when(riesgoRepository.findById(50L)).thenReturn(Optional.of(riesgo));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(i -> i.getArgument(0));

        Riesgo cancelado = riesgoCommandService.cancelarRiesgo(new CancelarRiesgoCommand(50L));

        assertEquals(EstadoRiesgo.CANCELADO, cancelado.getEstado());
        verify(eventPublisher, times(1)).publish(any());
    }
}
