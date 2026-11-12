package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.CancelPolicyCommand;
import com.seguros.polizas.application.command.CreatePolicyCommand;
import com.seguros.polizas.application.command.RenewPolicyCommand;
import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.domain.port.out.RiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Service Unit Tests: PolicyCommandService")
class PolicyCommandServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PolicyCommandService policyCommandService;

    private Policy activePolicy;
    private Policy cancelledPolicy;

    @BeforeEach
    void setUp() {
        activePolicy = new Policy(
                "POL-TEST-001",
                PolicyType.INDIVIDUAL,
                PolicyStatus.ACTIVE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                12,
                BigDecimal.valueOf(1000000.00),
                "PolicyHolder Test",
                "Insured Test",
                "Beneficiary Test"
        );
        activePolicy.setId(100L);

        Risk risk = new Risk(
                activePolicy,
                RiskStatus.ACTIVE,
                "Calle 123",
                "Bogota",
                "Insured Test",
                BigDecimal.valueOf(1000000.00)
        );
        risk.setId(200L);
        activePolicy.addRisk(risk);

        cancelledPolicy = new Policy(
                "POL-TEST-002",
                PolicyType.INDIVIDUAL,
                PolicyStatus.CANCELLED,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                12,
                BigDecimal.valueOf(800000.00),
                "PolicyHolder 2",
                "Insured 2",
                "Beneficiary 2"
        );
        cancelledPolicy.setId(101L);
    }

    @Test
    @DisplayName("renew active policy calculates IPC correctly, updates status and publishes event")
    void testRenewActivePolicySuccess() {
        when(policyRepository.findById(100L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Policy result = policyCommandService.renew(new RenewPolicyCommand(100L, BigDecimal.valueOf(5.00)));

        assertNotNull(result);
        assertEquals(PolicyStatus.RENEWED, result.getStatus());
        assertEquals(new BigDecimal("1050000.00"), result.getMonthlyRent());
        verify(policyRepository, times(1)).save(any(Policy.class));
        verify(eventPublisher, atLeastOnce()).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("renew cancelled policy throws BusinessRuleException")
    void testRenewCancelledPolicyThrowsBusinessRuleException() {
        when(policyRepository.findById(101L)).thenReturn(Optional.of(cancelledPolicy));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                policyCommandService.renew(new RenewPolicyCommand(101L, BigDecimal.valueOf(5.00)))
        );

        assertTrue(ex.getMessage().contains("Cannot renew a policy in CANCELLED status"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    @DisplayName("cancel policy updates status to CANCELLED and cascades to all risks")
    void testCancelPolicySuccess() {
        when(policyRepository.findById(100L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Policy result = policyCommandService.cancel(new CancelPolicyCommand(100L));

        assertNotNull(result);
        assertEquals(PolicyStatus.CANCELLED, result.getStatus());
        for (Risk r : result.getRisks()) {
            assertEquals(RiskStatus.CANCELLED, r.getStatus());
        }
        verify(policyRepository, times(1)).save(activePolicy);
        verify(eventPublisher, atLeastOnce()).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("create policy with duplicate policy number throws BusinessRuleException")
    void testCreateDuplicatePolicyNumberThrowsException() {
        when(policyRepository.existsByPolicyNumber("POL-DUPLICATE")).thenReturn(true);

        CreatePolicyCommand command = new CreatePolicyCommand(
                "POL-DUPLICATE",
                PolicyType.INDIVIDUAL,
                LocalDate.now(),
                12,
                BigDecimal.valueOf(1500000),
                "Holder",
                "Insured",
                "Beneficiary",
                null
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                policyCommandService.create(command)
        );

        assertTrue(ex.getMessage().contains("A policy already exists with number"));
        verify(policyRepository, never()).save(any(Policy.class));
    }
}
