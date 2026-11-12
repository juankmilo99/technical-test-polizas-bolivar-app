package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import com.seguros.polizas.infrastructure.persistence.entity.PolizaJpaEntity;
import com.seguros.polizas.infrastructure.persistence.entity.RiesgoJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador (outbound) que implementa el puerto {@link RiesgoRepository}.
 */
@Component
public class RiesgoRepositoryAdapter implements RiesgoRepository {

    private final RiesgoJpaRepository jpaRepository;
    private final PolizaJpaRepository polizaJpaRepository;

    public RiesgoRepositoryAdapter(RiesgoJpaRepository jpaRepository,
                                   PolizaJpaRepository polizaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.polizaJpaRepository = polizaJpaRepository;
    }

    @Override
    public Riesgo save(Riesgo riesgo) {
        RiesgoJpaEntity entity = new RiesgoJpaEntity(riesgo);
        if (riesgo.getPoliza() != null && riesgo.getPoliza().getId() != null) {
            PolizaJpaEntity polizaRef = polizaJpaRepository.getReferenceById(riesgo.getPoliza().getId());
            entity.setPoliza(polizaRef);
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Riesgo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Riesgo> findByPolizaId(Long polizaId) {
        return jpaRepository.findByPolizaId(polizaId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Riesgo toDomain(RiesgoJpaEntity entity) {
        Riesgo r = new Riesgo();
        r.setId(entity.getId());
        r.setEstado(entity.getEstado());
        r.setDireccionInmueble(entity.getDireccionInmueble());
        r.setCiudad(entity.getCiudad());
        r.setNombreAsegurado(entity.getNombreAsegurado());
        r.setValorAsegurado(entity.getValorAsegurado());
        if (entity.getPoliza() != null) {
            Poliza p = new Poliza();
            p.setId(entity.getPoliza().getId());
            r.setPoliza(p);
        }
        return r;
    }
}
