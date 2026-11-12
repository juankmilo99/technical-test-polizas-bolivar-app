package com.seguros.polizas.infrastructure.web.rest.dto;

import com.seguros.polizas.domain.enums.TipoPoliza;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PolizaRequest {

    @NotBlank(message = "El numero de poliza es obligatorio")
    private String numeroPoliza;

    @NotNull(message = "El tipo de poliza es obligatorio (INDIVIDUAL o COLECTIVA)")
    private TipoPoliza tipo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "Los meses de vigencia son obligatorios")
    @Min(value = 1, message = "Los meses de vigencia deben ser al menos 1")
    private Integer mesesVigencia;

    @NotNull(message = "El canon mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El canon mensual debe ser mayor a cero")
    private BigDecimal canonMensual;

    private String tomador;
    private String asegurado;
    private String beneficiario;

    private RiesgoRequest riesgoInicial;

    public PolizaRequest() {
    }

    public String getNumeroPoliza() {
        return numeroPoliza;
    }

    public void setNumeroPoliza(String numeroPoliza) {
        this.numeroPoliza = numeroPoliza;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public void setTipo(TipoPoliza tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getMesesVigencia() {
        return mesesVigencia;
    }

    public void setMesesVigencia(Integer mesesVigencia) {
        this.mesesVigencia = mesesVigencia;
    }

    public BigDecimal getCanonMensual() {
        return canonMensual;
    }

    public void setCanonMensual(BigDecimal canonMensual) {
        this.canonMensual = canonMensual;
    }

    public String getTomador() {
        return tomador;
    }

    public void setTomador(String tomador) {
        this.tomador = tomador;
    }

    public String getAsegurado() {
        return asegurado;
    }

    public void setAsegurado(String asegurado) {
        this.asegurado = asegurado;
    }

    public String getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(String beneficiario) {
        this.beneficiario = beneficiario;
    }

    public RiesgoRequest getRiesgoInicial() {
        return riesgoInicial;
    }

    public void setRiesgoInicial(RiesgoRequest riesgoInicial) {
        this.riesgoInicial = riesgoInicial;
    }
}

