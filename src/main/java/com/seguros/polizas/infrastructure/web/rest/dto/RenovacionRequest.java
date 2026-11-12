package com.seguros.polizas.infrastructure.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RenovacionRequest {

    @NotNull(message = "El valor del IPC es obligatorio")
    @DecimalMin(value = "0.01", message = "El incremento de IPC debe ser mayor a cero")
    private BigDecimal ipc;

    public RenovacionRequest() {
    }

    public RenovacionRequest(BigDecimal ipc) {
        this.ipc = ipc;
    }

    public BigDecimal getIpc() {
        return ipc;
    }

    public void setIpc(BigDecimal ipc) {
        this.ipc = ipc;
    }
}

