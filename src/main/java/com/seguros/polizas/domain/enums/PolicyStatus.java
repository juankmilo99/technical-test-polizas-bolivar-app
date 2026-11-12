package com.seguros.polizas.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Lifecycle states of an insurance policy.
 */
public enum PolicyStatus {
    ACTIVE,
    RENEWED,
    CANCELLED;

    @JsonCreator
    public static PolicyStatus fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "VIGENTE", "ACTIVE" -> ACTIVE;
            case "RENOVADA", "RENEWED" -> RENEWED;
            case "CANCELADA", "CANCELLED", "CANCELED" -> CANCELLED;
            default -> throw new IllegalArgumentException("Unknown PolicyStatus: " + value);
        };
    }
}
