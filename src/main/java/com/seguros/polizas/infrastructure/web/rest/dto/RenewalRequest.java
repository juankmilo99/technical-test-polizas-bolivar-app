package com.seguros.polizas.infrastructure.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RenewalRequest {

    @NotNull(message = "IPC adjustment percentage is mandatory")
    @DecimalMin(value = "0.01", message = "IPC percentage must be greater than zero")
    private BigDecimal ipc;

    public RenewalRequest() {
    }

    public RenewalRequest(BigDecimal ipc) {
        this.ipc = ipc;
    }

    public BigDecimal getIpc() {
        return ipc;
    }

    public void setIpc(BigDecimal ipc) {
        this.ipc = ipc;
    }
}

