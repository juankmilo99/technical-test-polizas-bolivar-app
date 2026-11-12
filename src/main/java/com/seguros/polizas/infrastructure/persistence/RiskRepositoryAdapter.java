package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.RiskRepository;
import com.seguros.polizas.infrastructure.persistence.entity.PolicyJpaEntity;
import com.seguros.polizas.infrastructure.persistence.entity.RiskJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Secondary adapter implementing outbound port {@link RiskRepository}.
 */
@Component
public class RiskRepositoryAdapter implements RiskRepository {

    private final RiskJpaRepository jpaRepository;

    public RiskRepositoryAdapter(RiskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Risk save(Risk risk) {
        RiskJpaEntity entity = RiskJpaEntity.fromDomain(risk);
        if (risk.getPolicy() != null) {
            PolicyJpaEntity policyEntity = new PolicyJpaEntity();
            policyEntity.setId(risk.getPolicy().getId());
            entity.setPolicy(policyEntity);
        }
        RiskJpaEntity saved = jpaRepository.save(entity);
        Risk domain = saved.toDomain();
        if (risk.getPolicy() != null) {
            domain.setPolicy(risk.getPolicy());
        }
        return domain;
    }

    @Override
    public Optional<Risk> findById(Long id) {
        return jpaRepository.findById(id).map(r -> {
            Risk domain = r.toDomain();
            if (r.getPolicy() != null) {
                Policy p = new Policy();
                p.setId(r.getPolicy().getId());
                p.setType(r.getPolicy().getType());
                p.setStatus(r.getPolicy().getStatus());
                domain.setPolicy(p);
            }
            return domain;
        });
    }

    @Override
    public List<Risk> findByPolicyId(Long policyId) {
        return jpaRepository.findByPolicyId(policyId).stream()
                .map(RiskJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}

