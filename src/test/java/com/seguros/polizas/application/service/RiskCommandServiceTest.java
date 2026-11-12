package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.AddRiskCommand;
import com.seguros.polizas.application.command.CancelRiskCommand;
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
@DisplayName("Application Service Unit Tests: RiskCommandService")
class RiskCommandServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RiskCommandService riskCommandService;

    private Policy individualPolicy;
    private Policy collectivePolicy;

    @BeforeEach
    void setUp() {
        individualPolicy = new Policy(
                "POL-IND-001", PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusMonths(12), 12,
                BigDecimal.valueOf(1500000), "Holder", "Insured", "Beneficiary");
        individualPolicy.setId(1L);

        collectivePolicy = new Policy(
                "POL-COL-001", PolicyType.COLLECTIVE, PolicyStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusMonths(12), 12,
                BigDecimal.valueOf(5000000), "RealEstate", "Multiple", "Owners");
        collectivePolicy.setId(2L);
    }

    @Test
    @DisplayName("addRisk to COLLECTIVE policy creates and persists the risk")
    void testAddRiskToCollectivePolicySuccess() {
        when(policyRepository.findById(2L)).thenReturn(Optional.of(collectivePolicy));
        when(riskRepository.save(any(Risk.class))).thenAnswer(invocation -> {
            Risk r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });

        AddRiskCommand command = new AddRiskCommand(
                2L, "Carrera 7 # 100-20", "Bogota", "Tenant Name", BigDecimal.valueOf(2000000)
        );

        Risk savedRisk = riskCommandService.addRisk(command);

        assertNotNull(savedRisk);
        assertEquals(50L, savedRisk.getId());
        assertEquals(RiskStatus.ACTIVE, savedRisk.getStatus());
        verify(riskRepository, times(1)).save(any(Risk.class));
        verify(eventPublisher, times(1)).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("addRisk to INDIVIDUAL policy throws BusinessRuleException (invariant: max 1 risk)")
    void testAddRiskToIndividualPolicyThrowsBusinessRuleException() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));

        AddRiskCommand command = new AddRiskCommand(
                1L, "Calle 10 # 5-20", "Bogota", "Tenant Name", BigDecimal.valueOf(1200000)
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                riskCommandService.addRisk(command)
        );

        assertTrue(ex.getMessage().contains("An individual policy can only have 1 risk"));
        verify(riskRepository, never()).save(any(Risk.class));
        verify(eventPublisher, never()).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("cancelRisk cancels risk individual status")
    void testCancelRiskSuccess() {
        Risk risk = new Risk(collectivePolicy, RiskStatus.ACTIVE, "Calle 50", "Medellin", "Insured", BigDecimal.TEN);
        risk.setId(77L);

        when(riskRepository.findById(77L)).thenReturn(Optional.of(risk));
        when(riskRepository.save(any(Risk.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Risk result = riskCommandService.cancelRisk(new CancelRiskCommand(77L));

        assertNotNull(result);
        assertEquals(RiskStatus.CANCELLED, result.getStatus());
        verify(riskRepository, times(1)).save(risk);
        verify(eventPublisher, times(1)).publish(any(DomainEvent.class));
    }
}

