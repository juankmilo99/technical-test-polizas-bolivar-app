package com.seguros.polizas.domain.exception;

/**
 * Exception thrown when a requested policy cannot be found.
 */
public class PolicyNotFoundException extends DomainException {

    public PolicyNotFoundException(String message) {
        super(message);
    }

    public PolicyNotFoundException(Long policyId) {
        super("Policy not found with ID: " + policyId);
    }
}

