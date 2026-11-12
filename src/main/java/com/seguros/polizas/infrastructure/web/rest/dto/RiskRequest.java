package com.seguros.polizas.infrastructure.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RiskRequest {

    @NotBlank(message = "Property address is mandatory")
    @JsonAlias("direccionInmueble")
    private String propertyAddress;

    @NotBlank(message = "City is mandatory")
    @JsonAlias("ciudad")
    private String city;

    @NotBlank(message = "Insured name is mandatory")
    @JsonAlias("nombreAsegurado")
    private String insuredName;

    @NotNull(message = "Insured amount is mandatory")
    @DecimalMin(value = "0.01", message = "Insured amount must be greater than zero")
    @JsonAlias("valorAsegurado")
    private BigDecimal insuredAmount;

    public RiskRequest() {
    }

    public RiskRequest(String propertyAddress, String city, String insuredName, BigDecimal insuredAmount) {
        this.propertyAddress = propertyAddress;
        this.city = city;
        this.insuredName = insuredName;
        this.insuredAmount = insuredAmount;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInsuredName() {
        return insuredName;
    }

    public void setInsuredName(String insuredName) {
        this.insuredName = insuredName;
    }

    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }

    public void setInsuredAmount(BigDecimal insuredAmount) {
        this.insuredAmount = insuredAmount;
    }

    // Legacy getters/setters
    public String getDireccionInmueble() { return propertyAddress; }
    public void setDireccionInmueble(String d) { this.propertyAddress = d; }
    public String getCiudad() { return city; }
    public void setCiudad(String c) { this.city = c; }
    public String getNombreAsegurado() { return insuredName; }
    public void setNombreAsegurado(String n) { this.insuredName = n; }
    public BigDecimal getValorAsegurado() { return insuredAmount; }
    public void setValorAsegurado(BigDecimal v) { this.insuredAmount = v; }
}
