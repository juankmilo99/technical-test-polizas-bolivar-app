package com.seguros.polizas.application.port.in;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;

import java.util.List;

/**
 * Puerto de entrada (CQRS - Query side) para consultas de lectura.
 */
public interface PolizaQuery {

    List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado);

    Poliza obtenerPorId(Long id);

    List<Riesgo> listarRiesgos(Long polizaId);
}
