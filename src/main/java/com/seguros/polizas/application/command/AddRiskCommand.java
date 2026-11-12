package com.seguros.polizas.application.command;

import java.math.BigDecimal;

/**
 * Command to add a risk to a collective policy.
 */
public record AddRiskCommand(
        Long policyId,
        String propertyAddress,
        String city,
        String insuredName,
        BigDecimal insuredAmount
) {
}
