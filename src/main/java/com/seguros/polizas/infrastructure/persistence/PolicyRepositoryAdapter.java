package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.infrastructure.persistence.entity.PolicyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Secondary adapter implementing outbound port {@link PolicyRepository}.
 */
@Component
public class PolicyRepositoryAdapter implements PolicyRepository {

    private final PolicyJpaRepository jpaRepository;

    public PolicyRepositoryAdapter(PolicyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Policy save(Policy policy) {
        PolicyJpaEntity entity = PolicyJpaEntity.fromDomain(policy);
        PolicyJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Policy> findById(Long id) {
        return jpaRepository.findById(id).map(PolicyJpaEntity::toDomain);
    }

    @Override
    public List<Policy> findAll() {
        return jpaRepository.findAll().stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findByTypeAndStatus(PolicyType type, PolicyStatus status) {
        return jpaRepository.findByTypeAndStatus(type, status).stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findByType(PolicyType type) {
        return jpaRepository.findByType(type).stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findByStatus(PolicyStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPolicyNumber(String policyNumber) {
        return jpaRepository.existsByPolicyNumber(policyNumber);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

