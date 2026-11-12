package com.seguros.polizas.infrastructure.web.rest.dto;

import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.enums.TipoPoliza;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PolizaResponse {

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
    private int cantidadRiesgos;
    private List<RiesgoResponse> riesgos = new ArrayList<>();

    public PolizaResponse() {
    }

    public static PolizaResponse fromEntity(Poliza p) {
        if (p == null) return null;
        PolizaResponse dto = new PolizaResponse();
        dto.setId(p.getId());
        dto.setNumeroPoliza(p.getNumeroPoliza());
        dto.setTipo(p.getTipo());
        dto.setEstado(p.getEstado());
        dto.setFechaInicio(p.getFechaInicio());
        dto.setFechaFin(p.getFechaFin());
        dto.setMesesVigencia(p.getMesesVigencia());
        dto.setCanonMensual(p.getCanonMensual());
        dto.setPrimaTotal(p.getPrimaTotal());
        dto.setTomador(p.getTomador());
        dto.setAsegurado(p.getAsegurado());
        dto.setBeneficiario(p.getBeneficiario());
        if (p.getRiesgos() != null) {
            dto.setCantidadRiesgos(p.getRiesgos().size());
            dto.setRiesgos(p.getRiesgos().stream()
                    .map(RiesgoResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

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

    public int getCantidadRiesgos() {
        return cantidadRiesgos;
    }

    public void setCantidadRiesgos(int cantidadRiesgos) {
        this.cantidadRiesgos = cantidadRiesgos;
    }

    public List<RiesgoResponse> getRiesgos() {
        return riesgos;
    }

    public void setRiesgos(List<RiesgoResponse> riesgos) {
        this.riesgos = riesgos;
    }
}

