package com.seguros.polizas.infrastructure.web.rest;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
