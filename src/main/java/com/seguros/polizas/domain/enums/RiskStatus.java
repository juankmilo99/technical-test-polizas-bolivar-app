package com.seguros.polizas.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Lifecycle states of an insured risk (property unit).
 */
public enum RiskStatus {
    ACTIVE,
    CANCELLED;

    @JsonCreator
    public static RiskStatus fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVO", "ACTIVE" -> ACTIVE;
            case "CANCELADO", "CANCELLED", "CANCELED" -> CANCELLED;
            default -> throw new IllegalArgumentException("Unknown RiskStatus: " + value);
        };
    }
}
