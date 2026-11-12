package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.infrastructure.persistence.entity.PolizaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad de persistencia de póliza.
 */
@Repository
public interface PolizaJpaRepository extends JpaRepository<PolizaJpaEntity, Long> {

    List<PolizaJpaEntity> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);

    List<PolizaJpaEntity> findByTipo(TipoPoliza tipo);

    List<PolizaJpaEntity> findByEstado(EstadoPoliza estado);

    Optional<PolizaJpaEntity> findByNumeroPoliza(String numeroPoliza);

    boolean existsByNumeroPoliza(String numeroPoliza);
}
