package com.seguros.polizas.infrastructure.persistence.entity;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad JPA de póliza (adapter de persistencia).
 *
 * <p>Separa el modelo de dominio (puro) del modelo relacional de la base de
 * datos. El mapeo hacia/desde la entidad de dominio {@link Poliza} se realiza
 * explícitamente para mantener el núcleo libre de acoplamiento JPA.</p>
 */
@Entity
@Table(name = "polizas")
public class PolizaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_poliza", nullable = false, unique = true)
    private String numeroPoliza;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPoliza estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "meses_vigencia", nullable = false)
    private Integer mesesVigencia;

    @Column(name = "canon_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(name = "prima_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal primaTotal;

    @Column(name = "tomador")
    private String tomador;

    @Column(name = "asegurado")
    private String asegurado;

    @Column(name = "beneficiario")
    private String beneficiario;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RiesgoJpaEntity> riesgos = new ArrayList<>();

    public PolizaJpaEntity() {
    }

    // ------------------------------------------------------------------
    // Mapeo dominio <-> persistencia
    // ------------------------------------------------------------------

    public static PolizaJpaEntity fromDomain(Poliza p) {
        PolizaJpaEntity e = new PolizaJpaEntity();
        e.id = p.getId();
        e.numeroPoliza = p.getNumeroPoliza();
        e.tipo = p.getTipo();
        e.estado = p.getEstado();
        e.fechaInicio = p.getFechaInicio();
        e.fechaFin = p.getFechaFin();
        e.mesesVigencia = p.getMesesVigencia();
        e.canonMensual = p.getCanonMensual();
        e.primaTotal = p.getPrimaTotal();
        e.tomador = p.getTomador();
        e.asegurado = p.getAsegurado();
        e.beneficiario = p.getBeneficiario();
        if (p.getRiesgos() != null) {
            e.riesgos = p.getRiesgos().stream()
                    .map(r -> RiesgoJpaEntity.fromDomain(r, e))
                    .collect(Collectors.toList());
        }
        return e;
    }

    public Poliza toDomain() {
        Poliza p = new Poliza();
        p.setId(this.id);
        p.setNumeroPoliza(this.numeroPoliza);
        p.setTipo(this.tipo);
        p.setEstado(this.estado);
        p.setFechaInicio(this.fechaInicio);
        p.setFechaFin(this.fechaFin);
        p.setMesesVigencia(this.mesesVigencia);
        p.setCanonMensual(this.canonMensual);
        p.setPrimaTotal(this.primaTotal);
        p.setTomador(this.tomador);
        p.setAsegurado(this.asegurado);
        p.setBeneficiario(this.beneficiario);

        List<Riesgo> domainRiesgos = this.riesgos.stream()
                .map(r -> r.toDomain(p))
                .collect(Collectors.toList());
        p.setRiesgos(domainRiesgos);
        return p;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public EstadoPoliza getEstado() {
        return estado;
    }

    public void setEstado(EstadoPoliza estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
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

    public BigDecimal getPrimaTotal() {
        return primaTotal;
    }

    public void setPrimaTotal(BigDecimal primaTotal) {
        this.primaTotal = primaTotal;
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

    public List<RiesgoJpaEntity> getRiesgos() {
        return riesgos;
    }

    public void setRiesgos(List<RiesgoJpaEntity> riesgos) {
        this.riesgos = riesgos;
    }
}
