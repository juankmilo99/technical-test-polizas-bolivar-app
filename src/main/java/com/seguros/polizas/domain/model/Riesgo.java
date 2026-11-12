package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.EstadoRiesgo;

import java.math.BigDecimal;

/**
 * Entidad de dominio Riesgo: unidad asegurable dentro de una póliza.
 * Modelo de dominio puro (sin anotaciones JPA).
 */
public class Riesgo {

    private Long id;
    private Poliza poliza;
    private EstadoRiesgo estado;
    private String direccionInmueble;
    private String ciudad;
    private String nombreAsegurado;
    private BigDecimal valorAsegurado;

    public Riesgo() {
    }

    public Riesgo(Poliza poliza, EstadoRiesgo estado, String direccionInmueble,
                  String ciudad, String nombreAsegurado, BigDecimal valorAsegurado) {
        this.poliza = poliza;
        this.estado = estado;
        this.direccionInmueble = direccionInmueble;
        this.ciudad = ciudad;
        this.nombreAsegurado = nombreAsegurado;
        this.valorAsegurado = valorAsegurado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public void setPoliza(Poliza poliza) {
        this.poliza = poliza;
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
