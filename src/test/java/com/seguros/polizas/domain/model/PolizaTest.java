package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Dominio: Entidad Poliza")
class PolizaTest {

    @Test
    @DisplayName("calcularPrima calcula correctamente canon * meses con redondeo HALF_UP")
    void testCalcularPrima() {
        BigDecimal canon = new BigDecimal("1250000.50");
        Integer meses = 12;

        BigDecimal prima = Poliza.calcularPrima(canon, meses);

        assertEquals(new BigDecimal("15000006.00"), prima);
    }

    @Test
    @DisplayName("calcularPrima con valores nulos retorna ZERO")
    void testCalcularPrimaValoresNulos() {
        assertEquals(BigDecimal.ZERO, Poliza.calcularPrima(null, 12));
        assertEquals(BigDecimal.ZERO, Poliza.calcularPrima(BigDecimal.TEN, null));
    }

    @ParameterizedTest(name = "Canon inicial: {0}, IPC: {1}%, Canon Esperado: {2}, Prima Esperada: {3}")
    @CsvSource({
            "1000000.00, 5.00, 1050000.00, 12600000.00",
            "2000000.00, 5.10, 2102000.00, 25224000.00",
            "1500000.00, 10.00, 1650000.00, 19800000.00",
            "800000.00, 3.25, 826000.00, 9912000.00"
    })
    @DisplayName("renovar aplica la formula oficial del IPC (+IPC%) e incrementa canon y prima")
    void testRenovarAplicaFormulaIpc(String canonInicial, String ipc, String canonEsperado, String primaEsperada) {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);
        Poliza poliza = new Poliza(
                "POL-TEST",
                TipoPoliza.INDIVIDUAL,
                EstadoPoliza.VIGENTE,
                inicio,
                fin,
                12,
                new BigDecimal(canonInicial),
                "Tomador",
                "Asegurado",
                "Beneficiario"
        );

        poliza.renovar(new BigDecimal(ipc));

        assertEquals(EstadoPoliza.RENOVADA, poliza.getEstado());
        assertEquals(new BigDecimal(canonEsperado), poliza.getCanonMensual());
        assertEquals(new BigDecimal(primaEsperada), poliza.getPrimaTotal());

        // Verifica desplazamiento de fechas de vigencia
        assertEquals(LocalDate.of(2026, 1, 1), poliza.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 31), poliza.getFechaFin());
    }

    @Test
    @DisplayName("renovar en poliza CANCELADA lanza IllegalStateException")
    void testRenovarPolizaCanceladaLanzaExcepcion() {
        Poliza poliza = new Poliza();
        poliza.setEstado(EstadoPoliza.CANCELADA);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                poliza.renovar(BigDecimal.valueOf(5.0))
        );

        assertTrue(ex.getMessage().contains("No se puede renovar una poliza cancelada"));
    }

    @Test
    @DisplayName("cancelar pasa la poliza y TODOS sus riesgos asociados a CANCELADO")
    void testCancelarCascadaRiesgos() {
        Poliza poliza = new Poliza();
        poliza.setEstado(EstadoPoliza.VIGENTE);

        Riesgo r1 = new Riesgo(poliza, EstadoRiesgo.ACTIVO, "Dir 1", "Bogota", "Asegurado 1", BigDecimal.TEN);
        Riesgo r2 = new Riesgo(poliza, EstadoRiesgo.ACTIVO, "Dir 2", "Medellin", "Asegurado 2", BigDecimal.TEN);

        poliza.addRiesgo(r1);
        poliza.addRiesgo(r2);

        poliza.cancelar();

        assertEquals(EstadoPoliza.CANCELADA, poliza.getEstado());
        assertEquals(EstadoRiesgo.CANCELADO, r1.getEstado());
        assertEquals(EstadoRiesgo.CANCELADO, r2.getEstado());
    }

    @Test
    @DisplayName("addRiesgo establece la relacion bidireccional correctamente")
    void testAddRiesgoRelacionBidireccional() {
        Poliza poliza = new Poliza();
        Riesgo riesgo = new Riesgo();

        poliza.addRiesgo(riesgo);

        assertEquals(1, poliza.getRiesgos().size());
        assertSame(poliza, riesgo.getPoliza());
    }
}

