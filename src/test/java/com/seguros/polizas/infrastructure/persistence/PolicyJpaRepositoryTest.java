package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.infrastructure.persistence.entity.PolicyJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Persistence Integration Tests: PolicyJpaRepository")
class PolicyJpaRepositoryTest {

    @Autowired
    private PolicyJpaRepository policyJpaRepository;

    private PolicyJpaEntity createTestPolicy(String number, PolicyType type, PolicyStatus status) {
        PolicyJpaEntity e = new PolicyJpaEntity();
        e.setPolicyNumber(number);
        e.setType(type);
        e.setStatus(status);
        e.setStartDate(LocalDate.now());
        e.setEndDate(LocalDate.now().plusMonths(12));
        e.setCoverageMonths(12);
        e.setMonthlyRent(BigDecimal.valueOf(1500000.00));
        e.setTotalPremium(BigDecimal.valueOf(18000000.00));
        e.setPolicyHolder("PolicyHolder");
        e.setInsured("Insured");
        e.setBeneficiary("Beneficiary");
        return e;
    }

    @Test
    @DisplayName("findByTypeAndStatus filters accurately based on criteria")
    void testFindByTypeAndStatus() {
        policyJpaRepository.save(createTestPolicy("POL-JPA-01", PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE));
        policyJpaRepository.save(createTestPolicy("POL-JPA-02", PolicyType.COLLECTIVE, PolicyStatus.ACTIVE));
        policyJpaRepository.save(createTestPolicy("POL-JPA-03", PolicyType.INDIVIDUAL, PolicyStatus.CANCELLED));

        List<PolicyJpaEntity> individualActive =
                policyJpaRepository.findByTypeAndStatus(PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE);

        assertTrue(individualActive.stream().anyMatch(p -> p.getPolicyNumber().equals("POL-JPA-01")));
        assertFalse(individualActive.stream().anyMatch(p -> p.getPolicyNumber().equals("POL-JPA-02")));
        assertFalse(individualActive.stream().anyMatch(p -> p.getPolicyNumber().equals("POL-JPA-03")));
    }

    @Test
    @DisplayName("existsByPolicyNumber checks existence accurately")
    void testExistsByPolicyNumber() {
        policyJpaRepository.save(createTestPolicy("POL-EXIST-01", PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE));

        assertTrue(policyJpaRepository.existsByPolicyNumber("POL-EXIST-01"));
        assertFalse(policyJpaRepository.existsByPolicyNumber("POL-NOT-EXIST"));
    }
}

