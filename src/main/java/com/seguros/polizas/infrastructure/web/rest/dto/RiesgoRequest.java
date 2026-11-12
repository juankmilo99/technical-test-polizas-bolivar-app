package com.seguros.polizas.infrastructure.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RiesgoRequest {

    @NotBlank(message = "La direccion del inmueble es obligatoria")
    private String direccionInmueble;

    private String ciudad;

    private String nombreAsegurado;

    @NotNull(message = "El valor asegurado es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor asegurado debe ser mayor a cero")
    private BigDecimal valorAsegurado;

    public RiesgoRequest() {
    }

    public RiesgoRequest(String direccionInmueble, String ciudad, String nombreAsegurado, BigDecimal valorAsegurado) {
        this.direccionInmueble = direccionInmueble;
        this.ciudad = ciudad;
        this.nombreAsegurado = nombreAsegurado;
        this.valorAsegurado = valorAsegurado;
    }

    public String getDireccionInmueble() {
        return direccionInmueble;
    }

    public void setDireccionInmueble(String direccionInmueble) {
        this.direccionInmueble = direccionInmueble;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getNombreAsegurado() {
        return nombreAsegurado;
    }

    public void setNombreAsegurado(String nombreAsegurado) {
        this.nombreAsegurado = nombreAsegurado;
    }

    public BigDecimal getValorAsegurado() {
        return valorAsegurado;
    }

    public void setValorAsegurado(BigDecimal valorAsegurado) {
        this.valorAsegurado = valorAsegurado;
    }
}

