package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.infrastructure.persistence.entity.RiesgoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad de persistencia de riesgo.
 */
@Repository
public interface RiesgoJpaRepository extends JpaRepository<RiesgoJpaEntity, Long> {

    List<RiesgoJpaEntity> findByPolizaId(Long polizaId);
}
