package com.seguros.polizas.infrastructure.web.rest.dto;

import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.model.Risk;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RiskResponse {

    private Long id;
    private Long policyId;
    private RiskStatus status;
    private String propertyAddress;
    private String city;
    private String insuredName;
    private BigDecimal insuredAmount;
    private LocalDate inclusionDate;
    private LocalDate cancellationDate;

    public RiskResponse() {
    }

    public static RiskResponse fromEntity(Risk r) {
        if (r == null) return null;
        RiskResponse dto = new RiskResponse();
        dto.setId(r.getId());
        if (r.getPolicy() != null) {
            dto.setPolicyId(r.getPolicy().getId());
        }
        dto.setStatus(r.getStatus());
        dto.setPropertyAddress(r.getPropertyAddress());
        dto.setCity(r.getCity());
        dto.setInsuredName(r.getInsuredName());
        dto.setInsuredAmount(r.getInsuredAmount());
        dto.setInclusionDate(r.getInclusionDate());
        dto.setCancellationDate(r.getCancellationDate());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public void setStatus(RiskStatus status) {
        this.status = status;
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

    public LocalDate getInclusionDate() {
        return inclusionDate;
    }

    public void setInclusionDate(LocalDate inclusionDate) {
        this.inclusionDate = inclusionDate;
    }

    public LocalDate getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    // Legacy getters for backward compatibility
    public Long getPolizaId() { return policyId; }
    public RiskStatus getEstado() { return status; }
    public String getDireccionInmueble() { return propertyAddress; }
    public String getNombreAsegurado() { return insuredName; }
    public BigDecimal getValorAsegurado() { return insuredAmount; }
    public LocalDate getFechaInclusion() { return inclusionDate; }
    public LocalDate getFechaCancelacion() { return cancellationDate; }
}
