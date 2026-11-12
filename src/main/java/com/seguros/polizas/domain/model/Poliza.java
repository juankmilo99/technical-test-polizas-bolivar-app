package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.PolizaCancelada;
import com.seguros.polizas.domain.event.PolizaRenovada;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root del dominio de pólizas.
 *
 * <p>Modelo de dominio puro: sin anotaciones JPA ni dependencias de
 * infraestructura. Encapsula las invariantes de negocio (cálculo de prima,
 * renovación por IPC, cancelación en cascada) y registra los eventos de
 * dominio que ocurren al mutar su estado.</p>
 */
public class Poliza {

    private Long id;
    private String numeroPoliza;
    private TipoPoliza tipo;
    private EstadoPoliza estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer mesesVigencia;
    private BigDecimal canonMensual;
    private BigDecimal primaTotal;
    private String tomador;
    private String asegurado;
    private String beneficiario;

    private final List<Riesgo> riesgos = new ArrayList<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Poliza() {
    }

    public Poliza(String numeroPoliza, TipoPoliza tipo, EstadoPoliza estado,
                  LocalDate fechaInicio, LocalDate fechaFin, Integer mesesVigencia,
                  BigDecimal canonMensual, String tomador, String asegurado, String beneficiario) {
        this.numeroPoliza = numeroPoliza;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.mesesVigencia = mesesVigencia;
        this.canonMensual = canonMensual;
        this.primaTotal = calcularPrima(canonMensual, mesesVigencia);
        this.tomador = tomador;
        this.asegurado = asegurado;
        this.beneficiario = beneficiario;
    }

    // ------------------------------------------------------------------
    // Lógica de dominio (invariantes)
    // ------------------------------------------------------------------

    public static BigDecimal calcularPrima(BigDecimal canon, Integer meses) {
        if (canon == null || meses == null) {
            return BigDecimal.ZERO;
        }
        return canon.multiply(BigDecimal.valueOf(meses)).setScale(2, RoundingMode.HALF_UP);
    }

    public void addRiesgo(Riesgo riesgo) {
        this.riesgos.add(riesgo);
        riesgo.setPoliza(this);
    }

    /**
     * Cancela la póliza y, en cascada, todos sus riesgos asociados.
     * Registra el evento {@link PolizaCancelada}.
     */
    public void cancelar() {
        this.estado = EstadoPoliza.CANCELADA;
        for (Riesgo r : this.riesgos) {
            r.setEstado(EstadoRiesgo.CANCELADO);
        }
        registerEvent(new PolizaCancelada(this.id));
    }

    /**
     * Renueva la póliza aplicando el IPC. Recalcula canon y prima,
     * desplaza el periodo de vigencia y registra {@link PolizaRenovada}.
     */
    public void renovar(BigDecimal ipc) {
        if (this.estado == EstadoPoliza.CANCELADA) {
            throw new IllegalStateException("No se puede renovar una poliza cancelada.");
        }

        BigDecimal canonAnterior = this.canonMensual;
        // Formula de ajuste: canonNuevo = canonActual * (1 + (ipc / 100))
        BigDecimal factor = BigDecimal.ONE.add(
                ipc.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        this.canonMensual = this.canonMensual.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        this.primaTotal = calcularPrima(this.canonMensual, this.mesesVigencia);
        this.estado = EstadoPoliza.RENOVADA;

        // Desplaza el periodo de vigencia
        LocalDate nuevaFechaInicio = this.fechaFin.plusDays(1);
        this.fechaInicio = nuevaFechaInicio;
        this.fechaFin = nuevaFechaInicio.plusMonths(this.mesesVigencia).minusDays(1);

        registerEvent(new PolizaRenovada(this.id, canonAnterior, this.canonMensual, ipc));
    }

    // ------------------------------------------------------------------
    // Gestión de eventos de dominio
    // ------------------------------------------------------------------

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /** Devuelve y limpia los eventos de dominio acumulados por el agregado. */
    public List<DomainEvent> drainEvents() {
        if (domainEvents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

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

    public List<Riesgo> getRiesgos() {
        return riesgos;
    }

    public void setRiesgos(List<Riesgo> riesgos) {
        this.riesgos.clear();
        if (riesgos != null) {
            this.riesgos.addAll(riesgos);
        }
    }
}
