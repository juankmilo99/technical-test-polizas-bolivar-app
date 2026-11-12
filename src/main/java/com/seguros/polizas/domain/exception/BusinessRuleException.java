package com.seguros.polizas.domain.exception;

/**
 * Violación de una regla de negocio (invariante del dominio).
 * Se traduce a HTTP 422 en la capa de adaptadores.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
