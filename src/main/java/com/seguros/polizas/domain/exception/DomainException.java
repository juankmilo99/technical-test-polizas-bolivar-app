package com.seguros.polizas.domain.exception;

/**
 * Base para las excepciones de dominio.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
