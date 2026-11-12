package com.seguros.polizas.infrastructure.persistence;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.infrastructure.persistence.entity.PolicyJpaEntity;
import com.seguros.polizas.infrastructure.persistence.entity.RiskJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Persistence Integration Tests: RiskJpaRepository")
class RiskJpaRepositoryTest {

    @Autowired
    private RiskJpaRepository riskJpaRepository;

    @Autowired
    private PolicyJpaRepository policyJpaRepository;

    private PolicyJpaEntity createPolicy(String number, PolicyType type) {
        PolicyJpaEntity e = new PolicyJpaEntity();
        e.setPolicyNumber(number);
        e.setType(type);
        e.setStatus(PolicyStatus.ACTIVE);
        e.setStartDate(LocalDate.now());
        e.setEndDate(LocalDate.now().plusMonths(12));
        e.setCoverageMonths(12);
        e.setMonthlyRent(BigDecimal.valueOf(3000000));
        e.setTotalPremium(BigDecimal.valueOf(36000000));
        e.setPolicyHolder("Holder");
        e.setInsured("Insured");
        e.setBeneficiary("Beneficiary");
        return policyJpaRepository.save(e);
    }

    @Test
    @DisplayName("findByPolicyId returns only risks belonging to that policy")
    void testFindByPolicyId() {
        PolicyJpaEntity policy1 = createPolicy("POL-RISK-01", PolicyType.COLLECTIVE);
        PolicyJpaEntity policy2 = createPolicy("POL-RISK-02", PolicyType.INDIVIDUAL);

        RiskJpaEntity r1 = new RiskJpaEntity();
        r1.setPolicy(policy1);
        r1.setStatus(RiskStatus.ACTIVE);
        r1.setPropertyAddress("Address 1");
        r1.setCity("Bogota");
        r1.setInsuredName("Insured 1");
        r1.setInsuredAmount(BigDecimal.valueOf(1000000));
        riskJpaRepository.save(r1);

        RiskJpaEntity r2 = new RiskJpaEntity();
        r2.setPolicy(policy2);
        r2.setStatus(RiskStatus.ACTIVE);
        r2.setPropertyAddress("Address 2");
        r2.setCity("Medellin");
        r2.setInsuredName("Insured 2");
        r2.setInsuredAmount(BigDecimal.valueOf(2000000));
        riskJpaRepository.save(r2);

        List<RiskJpaEntity> risksPolicy1 = riskJpaRepository.findByPolicyId(policy1.getId());

        assertEquals(1, risksPolicy1.size());
        assertEquals("Address 1", risksPolicy1.get(0).getPropertyAddress());
    }
}
