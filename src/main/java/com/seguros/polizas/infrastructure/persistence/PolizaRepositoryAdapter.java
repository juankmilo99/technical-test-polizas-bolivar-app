package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.infrastructure.persistence.entity.PolizaJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador (outbound) que implementa el puerto {@link PolizaRepository}
 * traduciendo entre el modelo de dominio y la entidad JPA.
 */
@Component
public class PolizaRepositoryAdapter implements PolizaRepository {

    private final PolizaJpaRepository jpaRepository;

    public PolizaRepositoryAdapter(PolizaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Poliza save(Poliza poliza) {
        PolizaJpaEntity saved = jpaRepository.save(PolizaJpaEntity.fromDomain(poliza));
        return saved.toDomain();
    }

    @Override
    public Optional<Poliza> findById(Long id) {
        return jpaRepository.findById(id).map(PolizaJpaEntity::toDomain);
    }

    @Override
    public Optional<Poliza> findByNumeroPoliza(String numeroPoliza) {
        return jpaRepository.findByNumeroPoliza(numeroPoliza).map(PolizaJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNumeroPoliza(String numeroPoliza) {
        return jpaRepository.existsByNumeroPoliza(numeroPoliza);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Poliza> findAll() {
        return jpaRepository.findAll().stream()
                .map(PolizaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Poliza> findByTipo(TipoPoliza tipo) {
        return jpaRepository.findByTipo(tipo).stream()
                .map(PolizaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Poliza> findByEstado(EstadoPoliza estado) {
        return jpaRepository.findByEstado(estado).stream()
                .map(PolizaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado) {
        return jpaRepository.findByTipoAndEstado(tipo, estado).stream()
                .map(PolizaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}
