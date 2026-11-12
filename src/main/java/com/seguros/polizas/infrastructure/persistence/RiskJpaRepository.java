package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.infrastructure.persistence.entity.RiskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskJpaRepository extends JpaRepository<RiskJpaEntity, Long> {

    List<RiskJpaEntity> findByPolicyId(Long policyId);
}
