package com.seguros.polizas.domain.port.out;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.model.Poliza;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (outbound) para persistencia del agregado Poliza.
 * La implementación JPA vive en infrastructure/persistence y desacopla
 * el dominio de la tecnología de almacenamiento.
 */
public interface PolizaRepository {

    Poliza save(Poliza poliza);

    Optional<Poliza> findById(Long id);

    Optional<Poliza> findByNumeroPoliza(String numeroPoliza);

    boolean existsByNumeroPoliza(String numeroPoliza);

    boolean existsById(Long id);

    List<Poliza> findAll();

    List<Poliza> findByTipo(TipoPoliza tipo);

    List<Poliza> findByEstado(EstadoPoliza estado);

    List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);
}
