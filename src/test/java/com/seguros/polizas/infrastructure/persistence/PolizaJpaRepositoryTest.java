package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.infrastructure.persistence.entity.PolizaJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Pruebas de Integración de Persistencia: PolizaJpaRepository")
class PolizaJpaRepositoryTest {

    @Autowired
    private PolizaJpaRepository polizaJpaRepository;

    private PolizaJpaEntity crearPolizaPrueba(String numero, TipoPoliza tipo, EstadoPoliza estado) {
        PolizaJpaEntity e = new PolizaJpaEntity();
        e.setNumeroPoliza(numero);
        e.setTipo(tipo);
        e.setEstado(estado);
        e.setFechaInicio(LocalDate.now());
        e.setFechaFin(LocalDate.now().plusMonths(12));
        e.setMesesVigencia(12);
        e.setCanonMensual(BigDecimal.valueOf(1500000.00));
        e.setPrimaTotal(BigDecimal.valueOf(18000000.00));
        e.setTomador("Tomador");
        e.setAsegurado("Asegurado");
        e.setBeneficiario("Beneficiario");
        return e;
    }

    @Test
    @DisplayName("findByTipoAndEstado filtra correctamente según los criterios")
    void testFindByTipoAndEstado() {
        polizaJpaRepository.save(crearPolizaPrueba("POL-JPA-01", TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE));
        polizaJpaRepository.save(crearPolizaPrueba("POL-JPA-02", TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE));
        polizaJpaRepository.save(crearPolizaPrueba("POL-JPA-03", TipoPoliza.INDIVIDUAL, EstadoPoliza.CANCELADA));

        List<PolizaJpaEntity> individualesVigentes =
                polizaJpaRepository.findByTipoAndEstado(TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE);

        assertTrue(individualesVigentes.stream().anyMatch(p -> p.getNumeroPoliza().equals("POL-JPA-01")));
        assertFalse(individualesVigentes.stream().anyMatch(p -> p.getNumeroPoliza().equals("POL-JPA-02")));
        assertFalse(individualesVigentes.stream().anyMatch(p -> p.getNumeroPoliza().equals("POL-JPA-03")));
    }

    @Test
    @DisplayName("existsByNumeroPoliza verifica existencia correctamente")
    void testExistsByNumeroPoliza() {
        polizaJpaRepository.save(crearPolizaPrueba("POL-EXISTE-01", TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE));

        assertTrue(polizaJpaRepository.existsByNumeroPoliza("POL-EXISTE-01"));
        assertFalse(polizaJpaRepository.existsByNumeroPoliza("POL-NO-EXISTE-99"));
    }

    @Test
    @DisplayName("Insertar número de póliza duplicado viola la restricción UNIQUE")
    void testRestriccionUnicaNumeroPoliza() {
        polizaJpaRepository.saveAndFlush(crearPolizaPrueba("POL-DUPLICADA", TipoPoliza.INDIVIDUAL, EstadoPoliza.VIGENTE));

        assertThrows(Exception.class, () ->
                polizaJpaRepository.saveAndFlush(crearPolizaPrueba("POL-DUPLICADA", TipoPoliza.COLECTIVA, EstadoPoliza.VIGENTE)));
    }
}
