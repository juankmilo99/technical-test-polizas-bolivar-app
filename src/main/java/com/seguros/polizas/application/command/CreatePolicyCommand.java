package com.seguros.polizas.application.command;

import com.seguros.polizas.domain.enums.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command (write model) to create a new insurance policy.
 */
public record CreatePolicyCommand(
        String policyNumber,
        PolicyType type,
        LocalDate startDate,
        Integer coverageMonths,
        BigDecimal monthlyRent,
        String policyHolder,
        String insured,
        String beneficiary,
        InitialRisk initialRisk
) {

    public record InitialRisk(
            String propertyAddress,
            String city,
            String insuredName,
            BigDecimal insuredAmount
    ) {
    }
}
