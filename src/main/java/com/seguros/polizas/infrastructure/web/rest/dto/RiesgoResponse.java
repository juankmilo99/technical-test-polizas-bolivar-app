package com.seguros.polizas.infrastructure.web.rest.dto;

import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.model.Riesgo;
import java.math.BigDecimal;

public class RiesgoResponse {

    private Long id;
    private Long polizaId;
    private EstadoRiesgo estado;
    private String direccionInmueble;
    private String ciudad;
    private String nombreAsegurado;
    private BigDecimal valorAsegurado;

    public RiesgoResponse() {
    }

    public static RiesgoResponse fromEntity(Riesgo r) {
        if (r == null) return null;
        RiesgoResponse dto = new RiesgoResponse();
        dto.setId(r.getId());
        if (r.getPoliza() != null) {
            dto.setPolizaId(r.getPoliza().getId());
        }
        dto.setEstado(r.getEstado());
        dto.setDireccionInmueble(r.getDireccionInmueble());
        dto.setCiudad(r.getCiudad());
        dto.setNombreAsegurado(r.getNombreAsegurado());
        dto.setValorAsegurado(r.getValorAsegurado());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolizaId() {
        return polizaId;
    }

    public void setPolizaId(Long polizaId) {
        this.polizaId = polizaId;
    }

    public EstadoRiesgo getEstado() {
        return estado;
    }

    public void setEstado(EstadoRiesgo estado) {
        this.estado = estado;
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

