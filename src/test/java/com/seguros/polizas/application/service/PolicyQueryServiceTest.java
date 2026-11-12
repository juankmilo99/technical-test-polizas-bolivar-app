package com.seguros.polizas.application.service;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.exception.PolicyNotFoundException;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import com.seguros.polizas.domain.port.out.PolicyRepository;
import com.seguros.polizas.domain.port.out.RiskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Query Service Unit Tests: PolicyQueryService")
class PolicyQueryServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RiskRepository riskRepository;

    @InjectMocks
    private PolicyQueryService policyQueryService;

    private Policy createPolicy(Long id, PolicyType type, PolicyStatus status) {
        Policy p = new Policy(
                "POL-" + id, type, status,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 12,
                BigDecimal.valueOf(1000000), "Holder", "Insured", "Beneficiary");
        p.setId(id);
        return p;
    }

    @Test
    @DisplayName("list without filters delegates to findAll")
    void testListWithoutFilters() {
        when(policyRepository.findAll()).thenReturn(List.of(createPolicy(1L, PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE)));

        List<Policy> result = policyQueryService.list(null, null);

        assertEquals(1, result.size());
        verify(policyRepository).findAll();
        verify(policyRepository, never()).findByType(any());
        verify(policyRepository, never()).findByStatus(any());
        verify(policyRepository, never()).findByTypeAndStatus(any(), any());
    }

    @Test
    @DisplayName("list with type and status filters delegates to findByTypeAndStatus")
    void testListWithBothFilters() {
        when(policyRepository.findByTypeAndStatus(PolicyType.COLLECTIVE, PolicyStatus.ACTIVE))
                .thenReturn(List.of(createPolicy(2L, PolicyType.COLLECTIVE, PolicyStatus.ACTIVE)));

        List<Policy> result = policyQueryService.list(PolicyType.COLLECTIVE, PolicyStatus.ACTIVE);

        assertEquals(1, result.size());
        verify(policyRepository).findByTypeAndStatus(PolicyType.COLLECTIVE, PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("getById returns policy when found")
    void testGetByIdFound() {
        Policy p = createPolicy(5L, PolicyType.INDIVIDUAL, PolicyStatus.ACTIVE);
        when(policyRepository.findById(5L)).thenReturn(Optional.of(p));

        Policy result = policyQueryService.getById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
    }

    @Test
    @DisplayName("getById throws PolicyNotFoundException when ID does not exist")
    void testGetByIdNotFound() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> policyQueryService.getById(999L));
    }

    @Test
    @DisplayName("listRisks returns associated risks")
    void testListRisks() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(createPolicy(1L, PolicyType.COLLECTIVE, PolicyStatus.ACTIVE)));
        when(riskRepository.findByPolicyId(1L)).thenReturn(List.of(new Risk()));

        List<Risk> risks = policyQueryService.listRisks(1L);

        assertEquals(1, risks.size());
    }
}

