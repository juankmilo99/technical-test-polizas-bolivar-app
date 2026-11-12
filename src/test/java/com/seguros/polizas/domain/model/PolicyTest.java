package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Unit Tests: Policy Aggregate Root")
class PolicyTest {

    @Test
    @DisplayName("calculatePremium calculates monthlyRent * months with HALF_UP rounding")
    void testCalculatePremium() {
        BigDecimal rent = new BigDecimal("1250000.50");
        Integer months = 12;

        BigDecimal premium = Policy.calculatePremium(rent, months);

        assertEquals(new BigDecimal("15000006.00"), premium);
    }

    @Test
    @DisplayName("calculatePremium returns ZERO when arguments are null")
    void testCalculatePremiumNullValues() {
        assertEquals(BigDecimal.ZERO, Policy.calculatePremium(null, 12));
        assertEquals(BigDecimal.ZERO, Policy.calculatePremium(BigDecimal.TEN, null));
    }

    @ParameterizedTest(name = "Initial rent: {0}, IPC: {1}%, Expected rent: {2}, Expected premium: {3}")
    @CsvSource({
            "1000000.00, 5.00, 1050000.00, 12600000.00",
            "2000000.00, 5.10, 2102000.00, 25224000.00",
            "1500000.00, 10.00, 1650000.00, 19800000.00",
            "800000.00, 3.25, 826000.00, 9912000.00"
    })
    @DisplayName("renew applies official IPC formula and updates rent and premium")
    void testRenewAppliesIpcFormula(String initialRent, String ipc, String expectedRent, String expectedPremium) {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        Policy policy = new Policy(
                "POL-TEST",
                PolicyType.INDIVIDUAL,
                PolicyStatus.ACTIVE,
                startDate,
                endDate,
                12,
                new BigDecimal(initialRent),
                "PolicyHolder",
                "Insured",
                "Beneficiary"
        );

        policy.renew(new BigDecimal(ipc));

        assertEquals(PolicyStatus.RENEWED, policy.getStatus());
        assertEquals(new BigDecimal(expectedRent), policy.getMonthlyRent());
        assertEquals(new BigDecimal(expectedPremium), policy.getTotalPremium());

        // Shift validity period forward
        assertEquals(LocalDate.of(2026, 1, 1), policy.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), policy.getEndDate());
    }

    @Test
    @DisplayName("renew throws IllegalStateException when policy is CANCELLED")
    void testRenewCancelledPolicyThrowsException() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.CANCELLED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                policy.renew(BigDecimal.valueOf(5.0))
        );

        assertTrue(ex.getMessage().contains("Cannot renew a cancelled policy"));
    }

    @Test
    @DisplayName("cancel cascades cancellation to all associated risks")
    void testCancelCascadeRisks() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.ACTIVE);

        Risk r1 = new Risk(policy, RiskStatus.ACTIVE, "Addr 1", "Bogota", "Insured 1", BigDecimal.TEN);
        Risk r2 = new Risk(policy, RiskStatus.ACTIVE, "Addr 2", "Medellin", "Insured 2", BigDecimal.TEN);

        policy.addRisk(r1);
        policy.addRisk(r2);

        policy.cancel();

        assertEquals(PolicyStatus.CANCELLED, policy.getStatus());
        assertEquals(RiskStatus.CANCELLED, r1.getStatus());
        assertEquals(RiskStatus.CANCELLED, r2.getStatus());
    }

    @Test
    @DisplayName("addRisk establishes bidirectional relationship correctly")
    void testAddRiskBidirectional() {
        Policy policy = new Policy();
        Risk risk = new Risk();

        policy.addRisk(risk);

        assertEquals(1, policy.getRisks().size());
        assertSame(policy, risk.getPolicy());
    }
}

