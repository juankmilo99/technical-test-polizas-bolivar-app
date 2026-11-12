package com.seguros.polizas.application.service;

import com.seguros.polizas.application.port.in.PolizaQuery;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del puerto de entrada {@link PolizaQuery} (CQRS - Query side).
 */
@Service
@Transactional(readOnly = true)
public class PolizaQueryService implements PolizaQuery {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;

    public PolizaQueryService(PolizaRepository polizaRepository,
                              RiesgoRepository riesgoRepository) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
    }

    @Override
    public List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado) {
        if (tipo != null && estado != null) {
            return polizaRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            return polizaRepository.findByTipo(tipo);
        } else if (estado != null) {
            return polizaRepository.findByEstado(estado);
        }
        return polizaRepository.findAll();
    }

    @Override
    public Poliza obtenerPorId(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new PolizaNotFoundException("Poliza no encontrada con ID: " + id));
    }

    @Override
    public List<Riesgo> listarRiesgos(Long polizaId) {
        if (!polizaRepository.existsById(polizaId)) {
            throw new PolizaNotFoundException("Poliza no encontrada con ID: " + polizaId);
        }
        return riesgoRepository.findByPolizaId(polizaId);
    }
}
