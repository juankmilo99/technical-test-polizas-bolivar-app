package com.seguros.polizas.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Type of rental policy under Colombian insurance regulations (Ley 820 de 2003).
 */
public enum PolicyType {
    INDIVIDUAL,
    COLLECTIVE;

    @JsonCreator
    public static PolicyType fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if ("COLECTIVA".equals(normalized) || "COLLECTIVE".equals(normalized)) {
            return COLLECTIVE;
        }
        if ("INDIVIDUAL".equals(normalized)) {
            return INDIVIDUAL;
        }
        throw new IllegalArgumentException("Unknown PolicyType: " + value);
    }
}

