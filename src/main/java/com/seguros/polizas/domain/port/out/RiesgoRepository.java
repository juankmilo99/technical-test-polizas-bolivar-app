package com.seguros.polizas.domain.port.out;

import com.seguros.polizas.domain.model.Riesgo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (outbound) para persistencia de la entidad Riesgo.
 */
public interface RiesgoRepository {

    Riesgo save(Riesgo riesgo);

    Optional<Riesgo> findById(Long id);

    List<Riesgo> findByPolizaId(Long polizaId);
}
