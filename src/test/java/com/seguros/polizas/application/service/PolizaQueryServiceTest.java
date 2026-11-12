package com.seguros.polizas.application.service;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias del lado de Lectura (CQRS - Query): PolizaQueryService")
class PolizaQueryServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @InjectMocks
    private PolizaQueryService polizaQueryService;

    private Poliza crearPoliza(Long id, TipoPoliza tipo, EstadoPoliza estado) {
        Poliza p = new Poliza(
                "POL-" + id, tipo, estado,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 12,
                BigDecimal.valueOf(1000000), "Tomador", "Asegurado", "Beneficiario");
        p.setId(id);
        return p;
    }

    @Test
    @DisplayName("listar sin filtros delega en findAll")
    void testListarSinFiltros() {
        when(polizaRepository.findAll()).thenReturn(List.of(crearPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE)));

        List<Poliza> resultado = polizaQueryService.listar(null, null);

        assertEquals(1, resultado.size());
        verify(polizaRepository).findAll();
        verify(polizaRepository, never()).findByTipo(any());
        verify(polizaRepository, never()).findByEstado(any());
        verify(polizaRepository, never()).findByTipoAndEstado(any(), any());
    }

    @Test
    @DisplayName("listar solo por tipo delega en findByTipo")
    void testListarSoloPorTipo() {
        when(polizaRepository.findByTipo(TipoPoliza.INDIVIDUAL))
                .thenReturn(List.of(crearPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE)));

        List<Poliza> resultado = polizaQueryService.listar(TipoPoliza.INDIVIDUAL, null);

        assertEquals(1, resultado.size());
        verify(polizaRepository).findByTipo(TipoPoliza.INDIVIDUAL);
    }

    @Test
    @DisplayName("listar solo por estado delega en findByEstado")
    void testListarSoloPorEstado() {
        when(polizaRepository.findByEstado(EstadoPoliza.VIGENTE))
                .thenReturn(List.of(crearPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE)));

        List<Poliza> resultado = polizaQueryService.listar(null, EstadoPoliza.VIGENTE);

        assertEquals(1, resultado.size());
        verify(polizaRepository).findByEstado(EstadoPoliza.VIGENTE);
    }

    @Test
    @DisplayName("listar por tipo y estado delega en findByTipoAndEstado")
    void testListarPorTipoYEstado() {
        when(polizaRepository.findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.RENOVADA))
                .thenReturn(List.of(crearPoliza(2L, TipoPoliza.COLECTIVA, EstadoPoliza.RENOVADA)));

        List<Poliza> resultado = polizaQueryService.listar(TipoPoliza.COLECTIVA, EstadoPoliza.RENOVADA);

        assertEquals(1, resultado.size());
        verify(polizaRepository).findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.RENOVADA);
    }

    @Test
    @DisplayName("obtenerPorId retorna la póliza cuando existe")
    void testObtenerPorIdExitoso() {
        Poliza p = crearPoliza(1L, TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(p));

        Poliza resultado = polizaQueryService.obtenerPorId(1L);

        assertSame(p, resultado);
    }

    @Test
    @DisplayName("obtenerPorId lanza PolizaNotFoundException cuando no existe")
    void testObtenerPorIdNoEncontrado() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PolizaNotFoundException.class, () -> polizaQueryService.obtenerPorId(99L));
        verify(polizaRepository).findById(99L);
    }

    @Test
    @DisplayName("listarRiesgos retorna los riesgos de una póliza existente")
    void testListarRiesgosExitoso() {
        Poliza p = crearPoliza(1L, TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE);
        Riesgo r1 = new Riesgo(p, com.seguros.polizas.domain.enums.EstadoRiesgo.ACTIVO,
                "Dir 1", "Bogota", "A", BigDecimal.valueOf(1000));
        Riesgo r2 = new Riesgo(p, com.seguros.polizas.domain.enums.EstadoRiesgo.ACTIVO,
                "Dir 2", "Bogota", "B", BigDecimal.valueOf(1000));

        when(polizaRepository.existsById(1L)).thenReturn(true);
        when(riesgoRepository.findByPolizaId(1L)).thenReturn(List.of(r1, r2));

        List<Riesgo> resultado = polizaQueryService.listarRiesgos(1L);

        assertEquals(2, resultado.size());
        verify(riesgoRepository).findByPolizaId(1L);
    }

    @Test
    @DisplayName("listarRiesgos lanza PolizaNotFoundException cuando la póliza no existe")
    void testListarRiesgosPolizaNoExiste() {
        when(polizaRepository.existsById(99L)).thenReturn(false);

        assertThrows(PolizaNotFoundException.class, () -> polizaQueryService.listarRiesgos(99L));
        verify(riesgoRepository, never()).findByPolizaId(any());
    }
}
