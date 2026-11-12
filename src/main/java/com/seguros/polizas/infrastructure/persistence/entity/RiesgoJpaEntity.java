package com.seguros.polizas.infrastructure.persistence.entity;

import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entidad JPA de riesgo (adapter de persistencia).
 */
@Entity
@Table(name = "riesgos")
public class RiesgoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poliza_id", nullable = false)
    @JsonIgnore
    private PolizaJpaEntity poliza;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoRiesgo estado;

    @Column(name = "direccion_inmueble", nullable = false)
    private String direccionInmueble;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "nombre_asegurado")
    private String nombreAsegurado;

    @Column(name = "valor_asegurado", precision = 15, scale = 2)
    private BigDecimal valorAsegurado;

    public RiesgoJpaEntity() {
    }

    public static RiesgoJpaEntity fromDomain(Riesgo r, PolizaJpaEntity polizaJpa) {
        RiesgoJpaEntity e = new RiesgoJpaEntity();
        e.id = r.getId();
        e.poliza = polizaJpa;
        e.estado = r.getEstado();
        e.direccionInmueble = r.getDireccionInmueble();
        e.ciudad = r.getCiudad();
        e.nombreAsegurado = r.getNombreAsegurado();
        e.valorAsegurado = r.getValorAsegurado();
        return e;
    }

    public RiesgoJpaEntity(Riesgo r) {
        this.id = r.getId();
        this.estado = r.getEstado();
        this.direccionInmueble = r.getDireccionInmueble();
        this.ciudad = r.getCiudad();
        this.nombreAsegurado = r.getNombreAsegurado();
        this.valorAsegurado = r.getValorAsegurado();
    }

    public Riesgo toDomain(Poliza domainPoliza) {
        Riesgo r = new Riesgo();
        r.setId(this.id);
        r.setPoliza(domainPoliza);
        r.setEstado(this.estado);
        r.setDireccionInmueble(this.direccionInmueble);
        r.setCiudad(this.ciudad);
        r.setNombreAsegurado(this.nombreAsegurado);
        r.setValorAsegurado(this.valorAsegurado);
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PolizaJpaEntity getPoliza() {
        return poliza;
    }

    public void setPoliza(PolizaJpaEntity poliza) {
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
