package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.infrastructure.persistence.entity.PolizaJpaEntity;
import com.seguros.polizas.infrastructure.persistence.entity.RiesgoJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Pruebas de Integración de Persistencia: RiesgoJpaRepository")
class RiesgoJpaRepositoryTest {

    @Autowired
    private RiesgoJpaRepository riesgoJpaRepository;

    @Autowired
    private PolizaJpaRepository polizaJpaRepository;

    private PolizaJpaEntity crearPoliza(String numero, TipoPoliza tipo) {
        PolizaJpaEntity e = new PolizaJpaEntity();
        e.setNumeroPoliza(numero);
        e.setTipo(tipo);
        e.setEstado(EstadoPoliza.VIGENTE);
        e.setFechaInicio(LocalDate.now());
        e.setFechaFin(LocalDate.now().plusMonths(12));
        e.setMesesVigencia(12);
        e.setCanonMensual(BigDecimal.valueOf(3000000));
        e.setPrimaTotal(BigDecimal.valueOf(36000000));
        e.setTomador("T");
        e.setAsegurado("A");
        e.setBeneficiario("B");
        return polizaJpaRepository.save(e);
    }

    @Test
    @DisplayName("findByPolizaId retorna únicamente los riesgos asociados a esa póliza")
    void testFindByPolizaId() {
        PolizaJpaEntity poliza1 = crearPoliza("POL-RIESGO-01", TipoPoliza.COLECTIVA);
        PolizaJpaEntity poliza2 = crearPoliza("POL-RIESGO-02", TipoPoliza.INDIVIDUAL);

        RiesgoJpaEntity r1 = new RiesgoJpaEntity();
        r1.setPoliza(poliza1);
        r1.setEstado(EstadoRiesgo.ACTIVO);
        r1.setDireccionInmueble("Dir 1");
        r1.setCiudad("Bogota");
        r1.setNombreAsegurado("Inquilino 1");
        r1.setValorAsegurado(BigDecimal.valueOf(1500000));

        RiesgoJpaEntity r2 = new RiesgoJpaEntity();
        r2.setPoliza(poliza1);
        r2.setEstado(EstadoRiesgo.ACTIVO);
        r2.setDireccionInmueble("Dir 2");
        r2.setCiudad("Bogota");
        r2.setNombreAsegurado("Inquilino 2");
        r2.setValorAsegurado(BigDecimal.valueOf(1500000));

        RiesgoJpaEntity r3 = new RiesgoJpaEntity();
        r3.setPoliza(poliza2);
        r3.setEstado(EstadoRiesgo.ACTIVO);
        r3.setDireccionInmueble("Dir 3");
        r3.setCiudad("Medellin");
        r3.setNombreAsegurado("Inquilino 3");
        r3.setValorAsegurado(BigDecimal.valueOf(1000000));

        riesgoJpaRepository.save(r1);
        riesgoJpaRepository.save(r2);
        riesgoJpaRepository.save(r3);

        List<RiesgoJpaEntity> riesgosP1 = riesgoJpaRepository.findByPolizaId(poliza1.getId());
        List<RiesgoJpaEntity> riesgosP2 = riesgoJpaRepository.findByPolizaId(poliza2.getId());

        assertEquals(2, riesgosP1.size());
        assertEquals(1, riesgosP2.size());
        assertTrue(riesgosP1.stream().anyMatch(r -> r.getDireccionInmueble().equals("Dir 1")));
        assertTrue(riesgosP1.stream().anyMatch(r -> r.getDireccionInmueble().equals("Dir 2")));
    }
}
