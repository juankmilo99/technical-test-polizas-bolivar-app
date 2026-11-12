package com.seguros.polizas.domain.exception;

/**
 * Recurso (póliza o riesgo) no encontrado.
 * Se traduce a HTTP 404 en la capa de adaptadores.
 */
public class PolizaNotFoundException extends DomainException {

    public PolizaNotFoundException(String message) {
        super(message);
    }
}
