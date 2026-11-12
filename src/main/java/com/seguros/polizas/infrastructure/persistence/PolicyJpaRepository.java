package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.infrastructure.persistence.entity.PolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyJpaRepository extends JpaRepository<PolicyJpaEntity, Long> {

    List<PolicyJpaEntity> findByTypeAndStatus(PolicyType type, PolicyStatus status);

    List<PolicyJpaEntity> findByType(PolicyType type);

    List<PolicyJpaEntity> findByStatus(PolicyStatus status);

    boolean existsByPolicyNumber(String policyNumber);
}
